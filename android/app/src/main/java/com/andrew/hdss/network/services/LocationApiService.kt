package com.andrew.hdss.network.services

import android.util.Log
import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.daos.LocationDao
import com.andrew.hdss.data.dtos.ErrorResponse
import com.andrew.hdss.data.dtos.LocationDto
import com.andrew.hdss.data.dtos.toEntity
import com.andrew.hdss.network.SyncResult
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LocationApiRepository {

    @GET("locations")
    suspend fun getChildren(
        @Query("id") parentId: Long? = null
    ): Response<List<LocationDto>>

    @GET("locations/{id}/descendants")
    suspend fun getDescendants(
        @Path("id") id: Long
    ): Response<List<LocationDto>>

    @GET("locations/{id}/ancestors")
    suspend fun getAncestors(
        @Path("id") id: Long
    ): Response<List<LocationDto>>
}

class LocationApiService(
    private val locationApiRepository: LocationApiRepository,
    private val locationDao: LocationDao,
    private val database: AppDatabase,
    private val json: Json
) {

    suspend fun fetchLocations(
        onProgress: (suspend (Int, Int) -> Unit)? = null
    ): SyncResult {
        val allDtos = mutableListOf<LocationDto>()

        val rootsResponse = try {
            locationApiRepository.getChildren(parentId = null)
        } catch (e: Exception) {
            Log.e("LocationApiService", "Failed to fetch root locations", e)
            return SyncResult.Error(e)
        }

        if (!rootsResponse.isSuccessful) {
            return rootsResponse.toFailureResult()
        }

        val roots = rootsResponse.body().orEmpty()
        allDtos += roots

        for (root in roots) {
            val descendantsResponse = try {
                locationApiRepository.getDescendants(root.id)
            } catch (e: Exception) {
                Log.e("LocationApiService", "Failed to fetch descendants of location id=${root.id}", e)
                return SyncResult.Error(e)
            }

            if (!descendantsResponse.isSuccessful) {
                return descendantsResponse.toFailureResult()
            }

            allDtos += descendantsResponse.body().orEmpty()
        }

        val entities = allDtos.mapNotNull { it.toEntity() }

        val entitiesSize = entities.size
        val currentLocationCount = locationDao.getCount().toInt()

        if(currentLocationCount == entitiesSize){
            return SyncResult.Success(entitiesSize)
        }

        database.withTransaction {
            locationDao.insertAll(entities)
        }

        return SyncResult.Success(entitiesSize)
    }

    private fun <T> Response<T>.toFailureResult(): SyncResult {
        val errorResponse = errorBody()?.string()?.let {
            try {
                json.decodeFromString<ErrorResponse>(it)
            } catch (e: Exception) {
                null
            }
        }

        return errorResponse?.let {
            SyncResult.Failure(it)
        }
            ?: SyncResult.Error(IllegalStateException("Unknown server error: HTTP ${code()}"))
    }
}