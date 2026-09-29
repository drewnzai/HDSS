package com.andrew.hdss.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.withTransaction
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.daos.AnswerDao
import com.andrew.hdss.data.daos.FormResponseDao
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.daos.MembershipDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.data.dtos.SyncItemResultDto
import com.andrew.hdss.data.dtos.SyncPushRequestDto
import com.andrew.hdss.data.dtos.SyncPushResponseDto
import com.andrew.hdss.data.models.Answer
import com.andrew.hdss.data.models.FormResponse
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.Visit
import com.andrew.hdss.data.models.enums.VisitStatus
import com.andrew.hdss.data.models.toPushDtos
import com.andrew.hdss.network.services.PushCallResult
import com.andrew.hdss.network.services.SyncPushService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty
import kotlin.collections.map

// Everything that will be sent for one visit. Lists hold only rows that
// aren't synced yet.
data class VisitBundle(
    val visit: Visit,
    val household: Household?,
    val individuals: List<Individual>,
    val memberships: List<Membership>,
    val formResponses: List<FormResponse>,
    val answers: List<Answer>
) {
    val hasPending: Boolean
        get() = !visit.synced || household != null || individuals.isNotEmpty() ||
                memberships.isNotEmpty() || formResponses.isNotEmpty() || answers.isNotEmpty()
}

data class PushFailure(val what: String, val message: String)

data class PushReport(
    val pushed: Map<String, Int>,
    val failures: List<PushFailure>
)

data class PushUiState(
    val isLoading: Boolean = true,
    val bundles: List<VisitBundle> = emptyList(),
    val selectedVisitId: String? = null,
    val isPushing: Boolean = false,
    val error: String? = null,
    val report: PushReport? = null
)

class PushDataViewModel(
    private val database: AppDatabase,
    private val visitDao: VisitDao,
    private val householdDao: HouseholdDao,
    private val individualDao: IndividualDao,
    private val membershipDao: MembershipDao,
    private val formResponseDao: FormResponseDao,
    private val answerDao: AnswerDao,
    private val pushService: SyncPushService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PushUiState())
    val uiState: StateFlow<PushUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { reloadBundles() }
    }

    fun select(visitId: String) {
        _uiState.update {
            it.copy(selectedVisitId = if (it.selectedVisitId == visitId) null else visitId)
        }
    }

    fun dismissReport() {
        _uiState.update { it.copy(report = null) }
    }

    fun push() {
        val state = _uiState.value
        if (state.isPushing) return
        val bundle = state.bundles.firstOrNull { it.visit.id == state.selectedVisitId } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPushing = true, error = null, report = null) }

            when (val result = pushService.push(bundle.toRequest())) {
                is PushCallResult.Failure ->
                    _uiState.update { it.copy(isPushing = false, error = result.message) }

                is PushCallResult.Success -> {
                    try {
                        val report = applyResults(bundle, result.response)
                        _uiState.update { it.copy(report = report) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not record push results locally", e)
                        _uiState.update {
                            it.copy(
                                error = "The server responded, but saving the result on this device " +
                                        "failed (${e.message}). Pushing again is safe."
                            )
                        }
                    }
                    reloadBundles()
                    _uiState.update { it.copy(isPushing = false) }
                }
            }
        }
    }

    private suspend fun reloadBundles() {
        val bundles = visitDao.getByStatus(VisitStatus.COMPLETED)
            .map { buildBundle(it) }
            .filter { it.hasPending }

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                bundles = bundles,
                selectedVisitId = state.selectedVisitId
                    ?.takeIf { id -> bundles.any { it.visit.id == id } }
            )
        }
    }

    private suspend fun buildBundle(visit: Visit): VisitBundle {
        val household = visit.householdClientId
            ?.let { householdDao.getByClientId(it) }
            ?.takeIf { !it.synced }

        val allMemberships = buildList {
            visit.householdClientId?.let { addAll(membershipDao.getByHousehold(it).first()) }
            visit.individualClientId?.let { addAll(membershipDao.getByIndividual(it).first()) }
        }.distinctBy { it.clientId }

        // Individuals come from ALL memberships (synced or not), then get
        // filtered, so an unsynced person is never missed because their
        // membership happened to sync first.
        val individualIds = (allMemberships.map { it.individualClientId } +
                listOfNotNull(visit.individualClientId)).distinct()
        val individuals = if (individualIds.isEmpty()) emptyList()
        else individualDao.getByClientIds(individualIds)

        val formResponses = formResponseDao.getByVisitId(visit.id)
        val answers = if (formResponses.isEmpty()) emptyList()
        else answerDao.getByFormResponseIds(formResponses.map { it.id })

        return VisitBundle(
            visit = visit,
            household = household,
            individuals = individuals.filter { !it.synced },
            memberships = allMemberships.filter { !it.synced },
            formResponses = formResponses.filter { !it.synced },
            answers = answers.filter { !it.synced }
        )
    }

    private fun VisitBundle.toRequest() = SyncPushRequestDto(
        households = listOfNotNull(household).toPushDtos(),
        individuals = individuals.toPushDtos(),
        memberships = memberships.toPushDtos(),
        visits = if (visit.synced) emptyList() else listOf(visit).toPushDtos(),
        formResponses = formResponses.toPushDtos(),
        answers = answers.toPushDtos()
    )

    private suspend fun applyResults(bundle: VisitBundle, response: SyncPushResponseDto): PushReport {
        val pushed = linkedMapOf<String, Int>()
        val failures = mutableListOf<PushFailure>()

        // Only items the server explicitly confirmed count as pushed. An item
        // with no result stays unsynced, so a response shape this code
        // doesn't understand shows up as failures instead of quietly marking
        // data as sent.
        fun reconcile(
            label: String,
            sentIds: List<String>,
            results: List<SyncItemResultDto>,
            needsServerId: Boolean
        ): List<SyncItemResultDto> {
            val byId = results.associateBy { it.clientId }
            val confirmed = mutableListOf<SyncItemResultDto>()
            for (clientId in sentIds) {
                val r = byId[clientId]
                when {
                    r == null ->
                        failures += PushFailure(label, "The server returned no result for this item.")
                    !r.isSuccess ->
                        failures += PushFailure(label, r.message ?: "Rejected by the server.")
                    needsServerId && r.id == null ->
                        failures += PushFailure(label, "The server accepted this item but sent no id for it.")
                    else -> confirmed += r
                }
            }
            if (confirmed.isNotEmpty()) pushed[label] = confirmed.size
            return confirmed
        }

        val households = reconcile(
            "Households", listOfNotNull(bundle.household?.clientId), response.households, true
        )
        val individuals = reconcile(
            "Individuals", bundle.individuals.map { it.clientId }, response.individuals, true
        )
        val memberships = reconcile(
            "Memberships", bundle.memberships.map { it.clientId }, response.memberships, true
        )
        val visits = reconcile(
            "Visits",
            if (bundle.visit.synced) emptyList() else listOf(bundle.visit.id),
            response.visitResults,
            false
        )
        val formResponses = reconcile(
            "Form responses", bundle.formResponses.map { it.id }, response.formResponseResults, false
        )
        val answers = reconcile(
            "Answers", bundle.answers.map { it.id }, response.answerResults, false
        )

        database.withTransaction {
            households.forEach { r -> r.id?.let { householdDao.markSynced(r.clientId, it) } }
            individuals.forEach { r -> r.id?.let { individualDao.markSynced(r.clientId, it) } }
            memberships.forEach { r -> r.id?.let { membershipDao.markSynced(r.clientId, it) } }
            visits.forEach { visitDao.markSynced(it.clientId) }
            if (formResponses.isNotEmpty()) formResponseDao.markSynced(formResponses.map { it.clientId })
            if (answers.isNotEmpty()) answerDao.markSynced(answers.map { it.clientId })
        }

        return PushReport(pushed, failures)
    }

    companion object {
        private const val TAG = "PushDataViewModel"

        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as HdssApplication
                val container = application.container
                val db = container.database
                PushDataViewModel(
                    database = db,
                    visitDao = db.visitDao(),
                    householdDao = db.householdDao(),
                    individualDao = db.individualDao(),
                    membershipDao = db.membershipDao(),
                    formResponseDao = db.formResponseDao(),
                    answerDao = db.answerDao(),
                    pushService = container.syncPushService
                )
            }
        }
    }
}