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

sealed interface FormSelector {
    data class ByName(val name: String) : FormSelector
    data class ById(val id: Long) : FormSelector
}

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
    val title: String = "",
    val errorMessage: String? = null
)

abstract class FormFlowViewModel(
    private val formSelector: FormSelector,
    private val formDao: FormDao,
    private val visitDao: VisitDao,
    private val visitHouseholdClientId: String?,
    private val visitIndividualClientId: String? = null
) : ViewModel() {

    val visitId: String = UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(FormFlowUiState())
    val uiState: StateFlow<FormFlowUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val form = when (val selector = formSelector) {
                    is FormSelector.ByName ->
                        formDao.getAll().firstOrNull { it.name == selector.name }
                    is FormSelector.ById ->
                        formDao.getById(selector.id)
                } ?: throw IllegalStateException(
                    "$formSelector not found — has it been downloaded?"
                )

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
                _uiState.update {
                    it.copy(
                        step = FormFlowStep.FORM,
                        formId = form.id,
                        title = form.title.ifBlank { form.name }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = FormFlowStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    abstract fun formContext(): FormFillContext

    fun onFormComplete() {
        _uiState.update { it.copy(step = FormFlowStep.DONE) }
    }

    suspend fun abandon(): Boolean = try {
        visitDao.deleteIfInProgress(visitId)
        true
    } catch (e: Exception) {
        Log.e("FormFlowViewModel", "Failed to clean up abandoned visit for $formSelector", e)
        false
    }
}