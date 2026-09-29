package com.andrew.hdss.network.services

import android.util.Log
import com.andrew.hdss.data.dtos.SyncPushRequestDto
import com.andrew.hdss.data.dtos.SyncPushResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.coroutines.cancellation.CancellationException

interface SyncApiRepository {
    @POST("sync/push")
    suspend fun push(@Body request: SyncPushRequestDto): Response<SyncPushResponseDto>
}

sealed interface PushCallResult {
    data class Success(val response: SyncPushResponseDto) : PushCallResult
    data class Failure(val message: String) : PushCallResult
}

class SyncPushService(private val api: SyncApiRepository) {

    suspend fun push(request: SyncPushRequestDto): PushCallResult {
        val response = try {
            api.push(request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return PushCallResult.Failure("Couldn't reach the server: ${e.message}")
        }

        if (!response.isSuccessful) {
            val body = response.errorBody()?.string()?.take(300)
            Log.e("SyncPushService", "Push failed: HTTP ${response.code()} body=$body")
            return PushCallResult.Failure("The server rejected the push (HTTP ${response.code()}).")
        }

        val body = response.body()
            ?: return PushCallResult.Failure("The server returned an empty response.")
        return PushCallResult.Success(body)
    }
}