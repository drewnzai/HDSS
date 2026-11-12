package com.andrew.hdss.ui.viewmodels;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.util.DeathInput;
import com.andrew.hdss.util.DeathResult;
import com.andrew.hdss.util.RecordDeathUseCase;
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DeathUiState {
    data object Idle : DeathUiState
    data object Saving : DeathUiState
    data object Saved : DeathUiState
    data class Error(val message: String) : DeathUiState
}

class RecordDeathViewModel(private val useCase:RecordDeathUseCase) : ViewModel() {

    private val _state = MutableStateFlow<DeathUiState>(DeathUiState.Idle)
            val state:
    StateFlow<DeathUiState> = _state.asStateFlow()

    fun submit(input:DeathInput) {
        if (_state.value == DeathUiState.Saving) return
        _state.value = DeathUiState.Saving

        viewModelScope.launch {
            _state.value = when (val result = useCase(input)) {
                DeathResult.Saved -> DeathUiState.Saved
                is DeathResult.Rejected -> DeathUiState.Error(result.message)
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as HdssApplication
                RecordDeathViewModel(RecordDeathUseCase(application.container.database))
            }
        }
    }
}