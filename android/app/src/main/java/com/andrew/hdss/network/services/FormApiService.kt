package com.andrew.hdss.network.services

import android.util.Log
import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.dtos.BatchResponse
import com.andrew.hdss.dtos.ErrorResponse
import com.andrew.hdss.dtos.FormDto
import com.andrew.hdss.dtos.ResourceRequest
import com.andrew.hdss.dtos.toEntities
import com.andrew.hdss.dtos.toQueryMap
import com.andrew.hdss.network.SyncResult
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface FormApiRepository{
    @GET("forms")
    suspend fun getForms(
        @QueryMap request: Map<String, String>
    ): Response<BatchResponse<FormDto>>

}

class FormApiService(
    private val formApiRepository: FormApiRepository,
    private val formDao: FormDao,
    private val database: AppDatabase,
    private val json: Json
) {
    private val pageSize = 500

    suspend fun fetchForms(
        onProgress: (suspend (downloaded: Int, total: Int) -> Unit)?
    ): SyncResult {
        val allDtos = mutableListOf<FormDto>()
        var totalElements: Int? = null
        var currentPage = 0

        while (true) {
            val response = try {
                formApiRepository.getForms(
                    ResourceRequest(page = currentPage, size = pageSize).toQueryMap()
                )
            } catch (e: Exception) {
                Log.e("FormApiService", "Failed to fetch individuals page=$currentPage", e)
                return SyncResult.NetworkError(e)
            }

            if (!response.isSuccessful) {
                return response.toFailureResult()
            }

            val batch = response.body()
                ?: return SyncResult.NetworkError(IllegalStateException("Empty response body"))

            if (totalElements == null) {
                totalElements = batch.totalElements?.toInt() ?: 0
            }

            allDtos += batch.data

            onProgress?.invoke(
                allDtos.size,
                totalElements
            )

            val totalPages = batch.totalPages ?: 1
            currentPage++
            if (currentPage >= totalPages) break
        }

        val entities = allDtos.toEntities()

        database.withTransaction {
            formDao.insertAll(entities)
        }

        return SyncResult.Success(entities.size)
    }

    private fun <T> Response<T>.toFailureResult(): SyncResult {
        val rawBody = errorBody()?.string()

        Log.e(
            "FormApiService",
            "Request failed: HTTP ${code()} ${message()} — url=${raw().request.url} body=$rawBody"
        )

        val errorResponse = rawBody?.let {
            try {
                json.decodeFromString<ErrorResponse>(it)
            } catch (e: Exception) {
                Log.w("FormApiService", "Error body did not match ErrorResponse shape", e)
                null
            }
        }

        return errorResponse?.let { SyncResult.Failure(it) }
            ?: SyncResult.NetworkError(IllegalStateException("Unknown server error: HTTP ${code()}"))
    }
}