package com.andrew.hdss.network

import com.andrew.hdss.data.dtos.ErrorResponse
import com.andrew.hdss.data.dtos.LoginResponse

sealed interface AuthResult {
    data class Success(
        val response: LoginResponse
    ) : AuthResult

    data class Failure(
        val error: ErrorResponse
    ) : AuthResult

    data class NetworkError(
        val exception: Throwable
    ) : AuthResult
}

sealed class SyncResult {
    data class Success(val count: Int) : SyncResult()
    data class Failure(val error: ErrorResponse) : SyncResult()
    data class NetworkError(val throwable: Throwable) : SyncResult()
}