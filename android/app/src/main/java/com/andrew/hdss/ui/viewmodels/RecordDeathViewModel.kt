package com.andrew.hdss.ui.viewmodels;

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
import com.andrew.hdss.data.models.enums.VisitStatus
import com.andrew.hdss.util.DeathInput;
import com.andrew.hdss.util.DeathResult;
import com.andrew.hdss.util.RecordDeathUseCase;
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

enum class DeathFlowStep { LOADING, FORM, DONE, ERROR }

data class DeathFlowUiState(
    val step: DeathFlowStep = DeathFlowStep.LOADING,
    val formId: Long? = null,
    val errorMessage: String? = null
)

class RecordDeathViewModel(
    private val householdClientId: String,
    private val deceasedClientId: String,
    private val formDao: FormDao,
    private val visitDao: VisitDao
) : ViewModel() {

    private val visitId: String = UUID.randomUUID().toString()
    private val visitStartedAt: LocalDateTime = LocalDateTime.now()

    private val _uiState = MutableStateFlow(DeathFlowUiState())
    val uiState: StateFlow<DeathFlowUiState> = _uiState.asStateFlow()

    val currentVisitId: String get() = visitId

    val formContext = FormFillContext(
        foreignKeys = mapOf("householdClientId" to householdClientId),
        presetAnswers = mapOf("deceased_clientId" to deceasedClientId)
    )

    init {
        viewModelScope.launch {
            try {
                val forms = formDao.getAll()
                val deathForm = forms.firstOrNull { it.name == RecordDeathUseCase.DEATH_FORM_NAME }
                    ?: throw IllegalStateException(
                        "Form '${RecordDeathUseCase.DEATH_FORM_NAME}' not found — has it been downloaded?"
                    )

                visitDao.insert(
                    Visit(
                        id = visitId,
                        householdClientId = householdClientId,
                        individualClientId = null,
                        visitDate = visitStartedAt,
                        status = VisitStatus.IN_PROGRESS,
                        synced = false
                    )
                )

                _uiState.update { it.copy(step = DeathFlowStep.FORM, formId = deathForm.id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = DeathFlowStep.ERROR, errorMessage = e.message) }
            }
        }
    }

    fun onFormComplete() {
        _uiState.update { it.copy(step = DeathFlowStep.DONE) }
    }

    suspend fun abandon(): Boolean = try {
        visitDao.deleteById(visitId)
        true
    } catch (e: Exception) {
        Log.e("RecordDeathViewModel", "Failed to clean up abandoned death form", e)
        false
    }

    companion object {
        fun factory(householdClientId: String, deceasedClientId: String) = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                RecordDeathViewModel(
                    householdClientId = householdClientId,
                    deceasedClientId = deceasedClientId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao()
                )
            }
        }
    }
}