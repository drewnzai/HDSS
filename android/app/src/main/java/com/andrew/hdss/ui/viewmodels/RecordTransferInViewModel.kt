package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.daos.MemberRow
import com.andrew.hdss.data.daos.MembershipDao
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.enums.RelationshipToHead
import com.andrew.hdss.util.usecases.RecordTransferInUseCase
import com.andrew.hdss.util.usecases.TransferInInput
import com.andrew.hdss.util.usecases.TransferInResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TransferInStep { SELECT_PERSON, DETAILS, DONE }

data class TransferInUiState(
    val step: TransferInStep = TransferInStep.SELECT_PERSON,
    val searchQuery: String = "",
    val candidates: List<Individual> = emptyList(),
    val selectedPerson: Individual? = null,
    val previousMembership: Membership? = null,
    val previousHouseholdMembers: List<MemberRow> = emptyList(),
    val destinationHasHead: Boolean = false,
    val transferDate: LocalDate = LocalDate.now(),
    val relationshipToHead: RelationshipToHead? = null,
    val otherMembersRemain: Boolean? = null,
    val previousNewHeadClientId: String? = null,
    val message: String? = null,
    val isSaving: Boolean = false
) {
    val wasHead: Boolean
        get() = previousMembership?.relationshipToHead == RelationshipToHead.HEAD

    val needsNewHead: Boolean
        get() = wasHead && otherMembersRemain == true
}

class RecordTransferInViewModel(
    private val destinationHouseholdClientId: String,
    private val individualDao: IndividualDao,
    private val membershipDao: MembershipDao,
    private val useCase: RecordTransferInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferInUiState())
    val uiState: StateFlow<TransferInUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var selectJob: Job? = null

    init {
        viewModelScope.launch {
            val members = membershipDao.getCurrentMembers(destinationHouseholdClientId).first()
            _uiState.update {
                it.copy(destinationHasHead = members.any { m -> m.relationshipToHead == RelationshipToHead.HEAD })
            }
        }
        search("")
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        search(query)
    }

    private fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val matches = individualDao.search(query).first()
            val eligible = matches.filter { individual ->
                val open = membershipDao.getCurrentMembership(individual.clientId)
                open != null && open.householdClientId != destinationHouseholdClientId
            }
            _uiState.update { it.copy(candidates = eligible) }
        }
    }

    fun selectPerson(individual: Individual) {
        selectJob?.cancel()
        selectJob = viewModelScope.launch {
            val membership = membershipDao.getCurrentMembership(individual.clientId)
            if (membership == null || membership.householdClientId == destinationHouseholdClientId) {
                _uiState.update { it.copy(message = "This person can't transfer in right now.") }
                return@launch
            }

            val previousMembers = membershipDao
                .observeOpenMembers(membership.householdClientId)
                .first()
                .filter { it.clientId != individual.clientId }

            _uiState.update {
                it.copy(
                    step = TransferInStep.DETAILS,
                    selectedPerson = individual,
                    previousMembership = membership,
                    previousHouseholdMembers = previousMembers,
                    relationshipToHead = null,
                    otherMembersRemain = null,
                    previousNewHeadClientId = null,
                    message = null
                )
            }
        }
    }

    fun backToSearch() {
        selectJob?.cancel()
        _uiState.update {
            it.copy(
                step = TransferInStep.SELECT_PERSON,
                selectedPerson = null,
                previousMembership = null,
                previousHouseholdMembers = emptyList(),
                relationshipToHead = null,
                otherMembersRemain = null,
                previousNewHeadClientId = null,
                message = null
            )
        }
    }

    fun onTransferDateChange(date: LocalDate) {
        _uiState.update { it.copy(transferDate = date, message = null) }
    }

    fun onRelationshipChange(relationship: RelationshipToHead) {
        _uiState.update { it.copy(relationshipToHead = relationship, message = null) }
    }

    fun onOtherMembersRemainChange(remain: Boolean) {
        _uiState.update {
            it.copy(
                otherMembersRemain = remain,
                previousNewHeadClientId = if (remain) it.previousNewHeadClientId else null,
                message = null
            )
        }
    }

    fun onPreviousNewHeadChange(clientId: String) {
        _uiState.update { it.copy(previousNewHeadClientId = clientId, message = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving || state.step != TransferInStep.DETAILS) return

        val person = state.selectedPerson ?: return
        val relationship = state.relationshipToHead
        if (relationship == null) {
            _uiState.update { it.copy(message = "Choose the relationship to head.") }
            return
        }
        if (state.wasHead && state.otherMembersRemain == null) {
            _uiState.update { it.copy(message = "Say whether other members remain in the previous household.") }
            return
        }
        if (state.needsNewHead && state.previousNewHeadClientId == null) {
            _uiState.update { it.copy(message = "Choose the new head of the previous household.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, message = null) }

        viewModelScope.launch {
            val result = useCase(
                TransferInInput(
                    personClientId = person.clientId,
                    destinationHouseholdClientId = destinationHouseholdClientId,
                    transferDate = state.transferDate,
                    relationshipToHead = relationship,
                    otherMembersRemain = state.otherMembersRemain == true,
                    previousNewHeadClientId = state.previousNewHeadClientId.takeIf { state.needsNewHead }
                )
            )
            _uiState.update {
                when (result) {
                    TransferInResult.Saved -> it.copy(step = TransferInStep.DONE, isSaving = false)
                    is TransferInResult.Rejected -> it.copy(isSaving = false, message = result.message)
                }
            }
        }
    }

    companion object {
        fun factory(destinationHouseholdClientId: String) = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                RecordTransferInViewModel(
                    destinationHouseholdClientId = destinationHouseholdClientId,
                    individualDao = db.individualDao(),
                    membershipDao = db.membershipDao(),
                    useCase = RecordTransferInUseCase(db)
                )
            }
        }
    }
}