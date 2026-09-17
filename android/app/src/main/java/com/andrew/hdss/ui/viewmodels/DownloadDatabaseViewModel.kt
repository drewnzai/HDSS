package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.ui.DownloadStepStatus
import com.andrew.hdss.worker.DownloadDatabaseWorker
import com.andrew.hdss.worker.WorkerStepState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json

data class DownloadStepUiState(
    val label: String,
    val status: DownloadStepStatus = DownloadStepStatus.Pending
)

data class DownloadDatabaseUiState(
    val steps: List<DownloadStepUiState> = listOf(),
    val isDownloading: Boolean = false
) {
    val canStartDownload: Boolean get() = !isDownloading
}

class DownloadDatabaseViewModel(
    private val workManager: WorkManager
) : ViewModel() {

    val uiState: StateFlow<DownloadDatabaseUiState> =
        workManager.getWorkInfosForUniqueWorkFlow(DownloadDatabaseWorker.WORK_NAME)
            .map { infos -> infos.firstOrNull()?.toUiState() ?: DownloadDatabaseUiState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DownloadDatabaseUiState()
            )

    fun startDownload() {
        val request = OneTimeWorkRequestBuilder<DownloadDatabaseWorker>().build()
        workManager.enqueueUniqueWork(
            DownloadDatabaseWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HdssApplication)
                DownloadDatabaseViewModel(WorkManager.getInstance(application))
            }
        }
    }

    private fun WorkInfo.toUiState(): DownloadDatabaseUiState {
        val data = if (state.isFinished) outputData else progress
        val json = data.getString(DownloadDatabaseWorker.KEY_STEPS_JSON)

        val stepStates: List<WorkerStepState> = json?.let { jsonString ->
            try {
                Json.decodeFromString<List<WorkerStepState>>(jsonString)
            } catch (e: Exception) {
                null
            }
        }.orEmpty()

        val steps = stepStates.map { it.toUiState() }

        return DownloadDatabaseUiState(
            steps = steps.ifEmpty { listOf(DownloadStepUiState(label = "Locations")) },
            isDownloading = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
        )
    }

private fun WorkerStepState.toUiState(): DownloadStepUiState = DownloadStepUiState(
    label = label,
    status = when (status) {
        "IN_PROGRESS" -> DownloadStepStatus.InProgress
        "SUCCESS" -> DownloadStepStatus.Success(count = count ?: 0)
        "FAILURE" -> DownloadStepStatus.Failure(message = message ?: "Unknown error")
        else -> DownloadStepStatus.Pending
    }
)
}