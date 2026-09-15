package com.andrew.hdss.network

import android.util.Log
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.dtos.ErrorResponse
import com.andrew.hdss.dtos.LoginRequest
import com.andrew.hdss.dtos.LoginResponse
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.time.ExperimentalTime

sealed interface LoginResult {
    data class Success(
        val response: LoginResponse
    ) : LoginResult

    data class Failure(
        val error: ErrorResponse
    ) : LoginResult

    data class NetworkError(
        val exception: Throwable
    ) : LoginResult
}

interface AuthApiRepository{
    @POST("auth/login")
    suspend fun login(
        @Body
        loginRequest: LoginRequest
    ): Response<LoginResponse>
}

class AuthApiService (
    private val authApiRepository: AuthApiRepository,
    private val tokenDataStore: TokenDataStore,
    private val json: Json
){
    @OptIn(ExperimentalTime::class)
    suspend fun login(
        loginRequest: LoginRequest
    ): LoginResult {

        return try {

            val response = authApiRepository.login(loginRequest)

            if (response.isSuccessful) {

                val loginResponse = response.body()

                if (loginResponse != null) {
                    tokenDataStore.saveSession(
                        accessToken = loginResponse.authenticationToken,
                        refreshToken = loginResponse.refreshToken,
                        firstName = loginResponse.firstName,
                        role = loginResponse.role,
                        username = loginResponse.username,
                        expiresAt = loginResponse.expiresAt.toString()
                    )

                    LoginResult.Success(loginResponse)
                } else {
                    LoginResult.NetworkError(
                        IllegalStateException(
                            "Server returned an empty response"
                        )
                    )
                }

            } else {

                val errorResponse = response.errorBody()
                    ?.string()
                    ?.let { errorBody ->
                        try {
                            json.decodeFromString<ErrorResponse>(errorBody)
                        } catch (e: Exception) {
                            null
                        }
                    }

                if (errorResponse != null) {
                    LoginResult.Failure(errorResponse)
                } else {
                    LoginResult.NetworkError(
                        IllegalStateException(
                            "Unknown server error: HTTP ${response.code()}"
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(
                "AuthApiService",
                "Login request failed",
                e
            )
            LoginResult.NetworkError(e)
        }
    }
}