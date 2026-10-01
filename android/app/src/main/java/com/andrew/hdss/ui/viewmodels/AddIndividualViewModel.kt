package com.andrew.hdss.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.data.models.Visit
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.VisitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

enum class AddIndividualStep { LOADING, FORM, DONE, ERROR }

data class AddIndividualUiState(
    val step: AddIndividualStep = AddIndividualStep.LOADING,
    val formId: Long? = null,
    val errorMessage: String? = null
)

class AddIndividualViewModel(
    private val householdClientId: String,
    private val formDao: FormDao,
    private val visitDao: VisitDao
) : ViewModel() {

    private val individualClientId: String = UUID.randomUUID().toString()
    private val membershipClientId: String = UUID.randomUUID().toString()
    private val visitId: String = UUID.randomUUID().toString()

    private var visit = Visit(
        id = visitId,
        householdClientId = householdClientId,
        individualClientId = null,
        visitDate = LocalDateTime.now(),
        status = VisitStatus.IN_PROGRESS,
        synced = false
    )

    private val _uiState = MutableStateFlow(AddIndividualUiState())
    val uiState: StateFlow<AddIndividualUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val forms = formDao.getAll()
                val form = forms.firstOrNull { it.name == "individual-registration" }
                    ?: throw IllegalStateException("Form 'individual-registration' not found — has it been downloaded?")

                visitDao.insert(visit)
                _uiState.update { it.copy(step = AddIndividualStep.FORM, formId = form.id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = AddIndividualStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    val formContext: FormFillContext
        get() = FormFillContext(
            entityClientIds = mapOf(
                MappedEntity.INDIVIDUAL to individualClientId,
                MappedEntity.MEMBERSHIP to membershipClientId
            ),
            foreignKeys = mapOf("householdClientId" to householdClientId)
        )

    val currentVisitId: String get() = visitId

    fun onFormComplete() {
        viewModelScope.launch {
            try {
                visit = visit.copy(status = VisitStatus.COMPLETED)
                visitDao.update(visit)
                _uiState.update { it.copy(step = AddIndividualStep.DONE) }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = AddIndividualStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    suspend fun abandon() {
        try {
            visitDao.deleteById(visitId)
        } catch (e: Exception) {
            Log.e("AddIndividualViewModel", "Failed to clean up abandoned add-individual visit", e)
        }
    }

    companion object {
        fun factory(householdClientId: String) = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as HdssApplication
                val db = application.container.database
                AddIndividualViewModel(
                    householdClientId = householdClientId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao()
                )
            }
        }
    }
}

private fun MutableStateFlow<AddIndividualUiState>.update(
    transform: (AddIndividualUiState) -> AddIndividualUiState
) {
    value = transform(value)
}