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
    private val visitDao: VisitDao
) : ViewModel() {

    // Generated once, upfront, per your decision that clientId
    // generation belongs in the ViewModel layer — reused consistently
    // across the Visit and both forms.
    val householdClientId: String = UUID.randomUUID().toString()
    private val individualClientId: String = UUID.randomUUID().toString()
    private val membershipClientId: String = UUID.randomUUID().toString()
    private val visitId: String = UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(BaselineFlowUiState())
    val uiState: StateFlow<BaselineFlowUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val householdForm = formDao.getByName("household-registration")
                val headForm = formDao.getByName("head-of-household-registration")

                visitDao.insert(
                    Visit(
                        id = visitId,
                        householdClientId = householdClientId,
                        individualClientId = null,
                        visitDate = LocalDateTime.now(),
                        status = VisitStatus.IN_PROGRESS,
                        synced = false
                    )
                )

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
            entityClientIds = mapOf(MappedEntity.HOUSEHOLD to householdClientId)
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
        _uiState.update { it.copy(step = BaselineFlowStep.HEAD_FORM) }
    }

    fun onHeadFormComplete() {
        viewModelScope.launch {
            // FLAG (unchanged from before): VisitDao still has no getById,
            // so this reconstructs the row by hand instead of reading it
            // back and copying it — fragile if Visit ever gains a field
            // this doesn't know about. Add VisitDao.getById(id) to fix
            // properly.
            visitDao.update(
                Visit(
                    id = visitId,
                    householdClientId = householdClientId,
                    individualClientId = null,
                    visitDate = LocalDateTime.now(),
                    status = VisitStatus.COMPLETED,
                    synced = false
                )
            )
        }
        _uiState.update { it.copy(step = BaselineFlowStep.DONE) }
    }

    companion object {
        fun factory(locationId: Long) = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as HdssApplication
                val db = application.container.database
                BaselineFlowViewModel(
                    locationId = locationId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao()
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