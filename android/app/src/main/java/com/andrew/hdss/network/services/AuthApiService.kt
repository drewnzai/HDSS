package com.andrew.hdss.network.services

import android.util.Log
import com.andrew.hdss.data.dtos.ErrorResponse
import com.andrew.hdss.data.dtos.LoginRequest
import com.andrew.hdss.data.dtos.LoginResponse
import com.andrew.hdss.data.dtos.RefreshTokenRequest
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.network.AuthResult
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
            AuthResult.Error(e)
        }
    }

    suspend fun refreshToken(): AuthResult {
        val currentRefreshToken = tokenDataStore.refreshToken.first()
        val currentUsername = tokenDataStore.username.first()

        if (currentRefreshToken == null || currentUsername == null) {
            return AuthResult.Error(
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
            AuthResult.Error(e)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun fallbackError(
        code: Int,
        path: String,
        message: String
    ): ErrorResponse = ErrorResponse(
        title = "HTTP $code",
        status = code,
        detail = message,
        path = path,
        timestamp = Clock.System.now()
    )

    private suspend fun handleAuthResponse(response: Response<LoginResponse>): AuthResult {
        if (response.isSuccessful) {
            val loginResponse = response.body()
                ?: return AuthResult.Error(IllegalStateException("Server returned an empty response"))

            try {
                tokenDataStore.saveSession(
                    accessToken = loginResponse.authenticationToken,
                    refreshToken = loginResponse.refreshToken,
                    username = loginResponse.username,
                    firstName = loginResponse.firstName,
                    role = loginResponse.role,
                    expiresAt = loginResponse.expiresAt
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AuthApiService", "Saving the session failed", e)
                return AuthResult.Error(IllegalStateException("Signed in, but the session couldn't be saved on this device."))
            }
            return AuthResult.Success(loginResponse)
        }

        val rawBody = response.errorBody()?.string()
        Log.w("AuthApiService", "Auth rejected: HTTP ${response.code()} body=$rawBody")

        val parsed = rawBody?.let {
            runCatching { json.decodeFromString<ErrorResponse>(it) }.getOrNull()
        }

        return AuthResult.Failure(
            parsed ?: fallbackError(
                code = response.code(),
                path = response.raw().request.url.encodedPath,
                message = when (response.code()) {
                    401, 403 -> "Incorrect username or password."
                    else -> "The server returned an error (HTTP ${response.code()})."
                }
            )
        )
    }
}