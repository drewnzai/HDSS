package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.network.services.LocationApiService
import com.andrew.hdss.network.SyncResult
import com.andrew.hdss.network.services.HouseholdApiService
import com.andrew.hdss.network.services.IndividualApiService
import com.andrew.hdss.ui.DownloadStepStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private data class DownloadStep(
    val label: String,
    val execute: suspend () -> SyncResult
)

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
    private val locationApiService: LocationApiService,
    private val individualApiService: IndividualApiService,
    private val householdApiService: HouseholdApiService
) : ViewModel() {

    private val steps: List<DownloadStep> = listOf(
        DownloadStep(
            label = "Locations",
            execute = { locationApiService.fetchLocations() }
        ),
        DownloadStep(
            label = "Individuals",
            execute = { individualApiService.fetchIndividuals() }
        ),
        DownloadStep(
            label = "Households",
            execute = { householdApiService.fetchHouseholds() }
        )
    )

    private val _uiState = MutableStateFlow(
        DownloadDatabaseUiState(steps = steps.map {
            step ->
            DownloadStepUiState(label = step.label)
        }
        )
    )
    val uiState: StateFlow<DownloadDatabaseUiState> = _uiState.asStateFlow()

    fun startDownload() {
        if (_uiState.value.isDownloading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                steps = steps.map { DownloadStepUiState(label = it.label) }
            )

            for (index in steps.indices) {
                updateStepStatus(index, DownloadStepStatus.InProgress)

                val result = steps[index].execute()

                val status = when (result) {
                    is SyncResult.Success -> DownloadStepStatus.Success(result.count)
                    is SyncResult.Failure -> DownloadStepStatus.Failure(result.error.detail)
                    is SyncResult.NetworkError -> DownloadStepStatus.Failure("Could not contact the server")
                }
                updateStepStatus(index, status)

                if (status is DownloadStepStatus.Failure) break
            }

            _uiState.value = _uiState.value.copy(isDownloading = false)
        }
    }

    private fun updateStepStatus(index: Int, status: DownloadStepStatus) {
        _uiState.value = _uiState.value.copy(
            steps = _uiState.value.steps.toMutableList().also {
                it[index] = it[index].copy(status = status)
            }
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HdssApplication)
                DownloadDatabaseViewModel(
                    locationApiService = application.container.locationApiService,
                    individualApiService = application.container.individualApiService,
                    householdApiService = application.container.householdApiService
                )
            }
        }
    }
}