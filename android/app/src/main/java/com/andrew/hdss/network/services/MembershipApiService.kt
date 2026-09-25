package com.andrew.hdss.network.services

import android.util.Log
import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.daos.MembershipDao
import com.andrew.hdss.data.dtos.BatchResponse
import com.andrew.hdss.data.dtos.ErrorResponse
import com.andrew.hdss.data.dtos.MembershipDto
import com.andrew.hdss.data.dtos.ResourceRequest
import com.andrew.hdss.data.dtos.toEntities
import com.andrew.hdss.data.dtos.toQueryMap
import com.andrew.hdss.network.SyncResult
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface MembershipApiRepository {

    @GET("memberships")
    suspend fun getMemberships(
        @QueryMap request: Map<String, String>
    ): Response<BatchResponse<MembershipDto>>
}

class MembershipApiService(
    private val membershipApiRepository: MembershipApiRepository,
    private val membershipDao: MembershipDao,
    private val individualDao: IndividualDao,
    private val householdDao: HouseholdDao,
    private val database: AppDatabase,
    private val json: Json
) {

    private val pageSize = 500

    suspend fun fetchMemberships(
        onProgress: (suspend (Int, Int) -> Unit)? = null
    ): SyncResult {
        val allDtos = mutableListOf<MembershipDto>()
        var totalElements: Int? = null
        var currentPage = 0

        while (true) {
            val response = try {
                membershipApiRepository.getMemberships(
                    ResourceRequest(page = currentPage, size = pageSize).toQueryMap()
                )
            } catch (e: Exception) {
                Log.e("MembershipApiService", "Failed to fetch memberships page=$currentPage", e)
                return SyncResult.Error(e)
            }

            if (!response.isSuccessful) {
                return response.toFailureResult()
            }

            val batch = response.body()
                ?: return SyncResult.Error(IllegalStateException("Empty response body"))

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

        val entities = allDtos.toEntities(individualDao, householdDao)

        database.withTransaction {
            membershipDao.insertAll(entities)
        }

        return SyncResult.Success(entities.size)
    }

    private fun <T> Response<T>.toFailureResult(): SyncResult {
        val rawBody = errorBody()?.string()

        Log.e(
            "MembershipApiService",
            "Request failed: HTTP ${code()} ${message()} — url=${raw().request.url} body=$rawBody"
        )

        val errorResponse = rawBody?.let {
            try {
                json.decodeFromString<ErrorResponse>(it)
            } catch (e: Exception) {
                Log.w("MembershipApiService", "Error body did not match ErrorResponse shape", e)
                null
            }
        }

        return errorResponse?.let { SyncResult.Failure(it) }
            ?: SyncResult.Error(IllegalStateException("Unknown server error: HTTP ${code()}"))
    }
}