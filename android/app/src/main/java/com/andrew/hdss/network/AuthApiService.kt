package com.andrew.hdss.network

import android.util.Log
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.dtos.ErrorResponse
import com.andrew.hdss.dtos.LoginRequest
import com.andrew.hdss.dtos.LoginResponse
import com.andrew.hdss.dtos.RefreshTokenRequest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiRepository{
    @POST("auth/login")
    suspend fun login(
        @Body
        loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(
        @Body
        refreshTokenRequest: RefreshTokenRequest
    ): Response<LoginResponse>
}

class AuthApiService(
    private val authApiRepository: AuthApiRepository,
    private val tokenDataStore: TokenDataStore,
    private val json: Json
) {
    suspend fun login(loginRequest: LoginRequest): AuthResult {
        return try {
            handleAuthResponse(authApiRepository.login(loginRequest))
        } catch (e: Exception) {
            Log.e("AuthApiService", "Login request failed", e)
            AuthResult.NetworkError(e)
        }
    }

    suspend fun refreshToken(): AuthResult {
        val currentRefreshToken = tokenDataStore.refreshToken.first()
        val currentUsername = tokenDataStore.username.first()

        if (currentRefreshToken == null || currentUsername == null) {
            return AuthResult.NetworkError(
                IllegalStateException("No stored session to refresh")
            )
        }

        return try {
            handleAuthResponse(
                authApiRepository.refresh(
                    RefreshTokenRequest(
                        token = currentRefreshToken,
                        username = currentUsername
                    )
                )
            )
        } catch (e: Exception) {
            Log.e("AuthApiService", "Token refresh failed", e)
            AuthResult.NetworkError(e)
        }
    }

    private fun handleAuthResponse(response: Response<LoginResponse>): AuthResult {
        if (response.isSuccessful) {
            val loginResponse = response.body()
            return if (loginResponse != null) {
                AuthResult.Success(loginResponse)
            } else {
                AuthResult.NetworkError(
                    IllegalStateException("Server returned an empty response")
                )
            }
        }

        val errorResponse = response.errorBody()
            ?.string()
            ?.let { errorBody ->
                try {
                    json.decodeFromString<ErrorResponse>(errorBody)
                } catch (e: Exception) {
                    null
                }
            }

        return if (errorResponse != null) {
            AuthResult.Failure(errorResponse)
        } else {
            AuthResult.NetworkError(
                IllegalStateException("Unknown server error: HTTP ${response.code()}")
            )
        }
    }
}