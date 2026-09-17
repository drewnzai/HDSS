package com.andrew.hdss.network.services

import android.util.Log
import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.daos.LocationDao
import com.andrew.hdss.dtos.BatchResponse
import com.andrew.hdss.dtos.ErrorResponse
import com.andrew.hdss.dtos.HouseholdDto
import com.andrew.hdss.dtos.ResourceRequest
import com.andrew.hdss.dtos.toEntities
import com.andrew.hdss.dtos.toQueryMap
import com.andrew.hdss.network.SyncResult
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface HouseholdApiRepository {

    @GET("api/households")
    suspend fun getHouseholds(
        @QueryMap request: Map<String, String>
    ): Response<BatchResponse<HouseholdDto>>
}

class HouseholdApiService(
    private val householdApiRepository: HouseholdApiRepository,
    private val householdDao: HouseholdDao,
    private val individualDao: IndividualDao,
    private val locationDao: LocationDao,
    private val database: AppDatabase,
    private val json: Json
) {

    private val pageSize = 500

    suspend fun fetchHouseholds(): SyncResult {
        val allDtos = mutableListOf<HouseholdDto>()
        var currentPage = 0

        while (true) {
            val response = try {
                householdApiRepository.getHouseholds(
                    ResourceRequest(page = currentPage, size = pageSize).toQueryMap()
                )
            } catch (e: Exception) {
                Log.e("HouseholdApiService", "Failed to fetch households page=$currentPage", e)
                return SyncResult.NetworkError(e)
            }

            if (!response.isSuccessful) {
                return response.toFailureResult()
            }

            val batch = response.body()
                ?: return SyncResult.NetworkError(IllegalStateException("Empty response body"))

            allDtos += batch.data

            val totalPages = batch.totalPages ?: 1
            currentPage++
            if (currentPage >= totalPages) break
        }

        val entities = allDtos.toEntities(individualDao, locationDao)

        database.withTransaction {
            householdDao.insertAll(entities)
        }

        return SyncResult.Success(entities.size)
    }

    private fun <T> Response<T>.toFailureResult(): SyncResult {
        val rawBody = errorBody()?.string()

        Log.e(
            "HouseholdApiService",
            "Request failed: HTTP ${code()} ${message()} — url=${raw().request.url} body=$rawBody"
        )

        val errorResponse = rawBody?.let {
            try {
                json.decodeFromString<ErrorResponse>(it)
            } catch (e: Exception) {
                Log.w("HouseholdApiService", "Error body did not match ErrorResponse shape", e)
                null
            }
        }

        return errorResponse?.let { SyncResult.Failure(it) }
            ?: SyncResult.NetworkError(IllegalStateException("Unknown server error: HTTP ${code()}"))
    }
}