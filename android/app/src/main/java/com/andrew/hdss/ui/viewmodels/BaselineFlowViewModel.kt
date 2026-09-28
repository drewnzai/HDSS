package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.data.models.Visit
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.VisitStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.LocationDao
import com.andrew.hdss.util.HouseholdCodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

enum class BaselineFlowStep { LOADING, HOUSEHOLD_FORM, HEAD_FORM, DONE, ERROR }

data class BaselineFlowUiState(
    val step: BaselineFlowStep = BaselineFlowStep.LOADING,
    val householdFormId: Long? = null,
    val headFormId: Long? = null,
    val errorMessage: String? = null
)

class BaselineFlowViewModel(
    private val locationId: Long,
    private val formDao: FormDao,
    private val visitDao: VisitDao,
    private val locationDao: LocationDao,
    private val householdDao: HouseholdDao
) : ViewModel() {

    val householdClientId: String = UUID.randomUUID().toString()
    private val individualClientId: String = UUID.randomUUID().toString()
    private val membershipClientId: String = UUID.randomUUID().toString()
    private val visitId: String = UUID.randomUUID().toString()

    // Set during init, before the first form is shown.
    private lateinit var householdCode: String

    // The Visit can't reference the household yet: the household row
    // doesn't exist until the household form is submitted, and that
    // form's FormResponse needs the Visit first. It starts with no
    // household and is linked in onHouseholdFormComplete().
    private var visit = Visit(
        id = visitId,
        householdClientId = null,
        individualClientId = null,
        visitDate = LocalDateTime.now(),
        status = VisitStatus.IN_PROGRESS,
        synced = false
    )

    private val _uiState = MutableStateFlow(BaselineFlowUiState())
    val uiState: StateFlow<BaselineFlowUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val forms = formDao.getAll()
                val householdForm = forms.firstOrNull { it.name == "household-registration" }
                    ?: throw IllegalStateException("Form 'household-registration' not found — has it been downloaded?")
                val headForm = forms.firstOrNull { it.name == "head-of-household-registration" }
                    ?: throw IllegalStateException("Form 'head-of-household-registration' not found — has it been downloaded?")

                householdCode = HouseholdCodeGenerator(householdDao).next(pathNamesTo(locationId))

                visitDao.insert(visit)

                _uiState.update {
                    it.copy(
                        step = BaselineFlowStep.HOUSEHOLD_FORM,
                        householdFormId = householdForm.id,
                        headFormId = headForm.id
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = BaselineFlowStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    val householdFormContext: FormFillContext
        get() = FormFillContext(
            locationId = locationId,
            entityClientIds = mapOf(MappedEntity.HOUSEHOLD to householdClientId),
            generatedFields = mapOf(
                MappedEntity.HOUSEHOLD to mapOf("householdCode" to householdCode)
            )
        )

    val headFormContext: FormFillContext
        get() = FormFillContext(
            entityClientIds = mapOf(
                MappedEntity.INDIVIDUAL to individualClientId,
                MappedEntity.MEMBERSHIP to membershipClientId
            ),
            foreignKeys = mapOf("householdClientId" to householdClientId)
        )

    val currentVisitId: String get() = visitId

    fun onHouseholdFormComplete() {
        viewModelScope.launch {
            try {
                visit = visit.copy(householdClientId = householdClientId)
                visitDao.update(visit)
                _uiState.update { it.copy(step = BaselineFlowStep.HEAD_FORM) }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = BaselineFlowStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    fun onHeadFormComplete() {
        viewModelScope.launch {
            try {
                visit = visit.copy(status = VisitStatus.COMPLETED)
                visitDao.update(visit)
                _uiState.update { it.copy(step = BaselineFlowStep.DONE) }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = BaselineFlowStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    // Names from the root down to the selected location, e.g.
// ["Nairobi", "Embakasi", "Kayole"], found by walking up through parentId.
// ASSUMPTION: Location has a nullable `parentId`. I haven't seen the
// entity, so adjust the name if yours differs.
    private suspend fun pathNamesTo(locationId: Long): List<String> {
        val names = mutableListOf<String>()
        var current = locationDao.getById(locationId)
            ?: throw IllegalStateException("Location $locationId not found — has the database been downloaded?")
        var depth = 0

        while (true) {
            check(++depth <= MAX_TREE_DEPTH) {
                "Location tree is deeper than $MAX_TREE_DEPTH levels, or contains a cycle"
            }
            names += current.name
            val parentId = current.parentId ?: break
            current = locationDao.getById(parentId)
                ?: throw IllegalStateException("Parent location $parentId not found")
        }

        return names.reversed()
    }

    companion object {
        private const val MAX_TREE_DEPTH = 20
        fun factory(locationId: Long) = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as HdssApplication
                val db = application.container.database
                BaselineFlowViewModel(
                    locationId = locationId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao(),
                    locationDao = db.locationDao(),
                    householdDao = db.householdDao()
                )
            }
        }
    }
}

private fun MutableStateFlow<BaselineFlowUiState>.update(
    transform: (BaselineFlowUiState) -> BaselineFlowUiState
) {
    value = transform(value)
}