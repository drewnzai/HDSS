package com.andrew.hdss.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.data.models.Visit
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.VisitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

enum class FormFlowStep { LOADING, FORM, DONE, ERROR }

data class FormFillContext(
    val locationId: Long? = null,
    val entityClientIds: Map<MappedEntity, String> = emptyMap(),
    val foreignKeys: Map<String, String> = emptyMap(),
    val generatedFields: Map<MappedEntity, Map<String, String>> = emptyMap(),
    val presetAnswers: Map<String, String> = emptyMap()
)

data class FormFlowUiState(
    val step: FormFlowStep = FormFlowStep.LOADING,
    val formId: Long? = null,
    val errorMessage: String? = null
)

// Shared lifecycle for single-form flows. Subclasses supply the form name,
// the visit's household or individual, and the form context.
abstract class FormFlowViewModel(
    private val formName: String,
    private val formDao: FormDao,
    private val visitDao: VisitDao,
    private val visitHouseholdClientId: String?,
    private val visitIndividualClientId: String? = null
) : ViewModel() {

    // Declared before init, so it's set when init runs.
    val visitId: String = UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(FormFlowUiState())
    val uiState: StateFlow<FormFlowUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val form = formDao.getAll().firstOrNull { it.name == formName }
                    ?: throw IllegalStateException("Form '$formName' not found — has it been downloaded?")

                visitDao.insert(
                    Visit(
                        id = visitId,
                        householdClientId = visitHouseholdClientId,
                        individualClientId = visitIndividualClientId,
                        visitDate = LocalDateTime.now(),
                        status = VisitStatus.IN_PROGRESS,
                        synced = false
                    )
                )
                _uiState.update { it.copy(step = FormFlowStep.FORM, formId = form.id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = FormFlowStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    abstract fun formContext(): FormFillContext

    // The form's submit path completes the visit, so this only moves the flow on.
    fun onFormComplete() {
        _uiState.update { it.copy(step = FormFlowStep.DONE) }
    }

    // Returns false if the unfinished visit couldn't be removed.
    suspend fun abandon(): Boolean = try {
        visitDao.deleteById(visitId)
        true
    } catch (e: Exception) {
        Log.e("FormFlowViewModel", "Failed to clean up abandoned visit for $formName", e)
        false
    }
}