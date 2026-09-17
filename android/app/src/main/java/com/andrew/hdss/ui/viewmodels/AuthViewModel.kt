package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.dtos.LoginRequest
import com.andrew.hdss.dtos.LoginResponse
import com.andrew.hdss.network.services.AuthApiService
import com.andrew.hdss.network.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val loginResponse: LoginResponse? = null
)


class AuthViewModel(
    private val authApiService: AuthApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username.trim(),
            errorMessage = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password.trim(),
            errorMessage = null
        )
    }

    fun login() {

        val currentState = _uiState.value

        if (currentState.username.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Username is required"
            )
            return
        }

        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Password is required"
            )
            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val request = LoginRequest(
                username = currentState.username,
                password = currentState.password
            )

            when (val result = authApiService.login(request)) {

                is AuthResult.Success -> {

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        loginResponse = result.response
                    )
                }

                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.detail
                    )
                }

                is AuthResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Could not contact the server"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    fun reset() {
        _uiState.value = LoginUiState()
    }

    suspend fun refreshToken(): AuthResult {
        return authApiService.refreshToken()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HdssApplication)
                val loginService = application.container.authApiService
                AuthViewModel(loginService)
            }
        }
    }
}