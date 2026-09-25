package com.andrew.hdss.network.services

import android.util.Log
import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.daos.ChoiceDao
import com.andrew.hdss.data.dtos.ChoiceDto
import com.andrew.hdss.data.dtos.ErrorResponse
import com.andrew.hdss.data.dtos.toEntities
import com.andrew.hdss.network.SyncResult
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ChoiceApiRepository{
    @GET("/choices/list/{listName")
    suspend fun getChoicesByListName(
        @Path("listName") listName: String
    ): Response<List<ChoiceDto>>

    @GET("/choices/list-names")
    suspend fun getListNames(): Response<List<String>>
}

class ChoiceApiService(
    private val choiceApiRepository: ChoiceApiRepository,
    private val choiceDao: ChoiceDao,
    private val database: AppDatabase,
    private val json: Json
) {
    suspend fun getChoices(
        onProgress: (suspend (downloaded: Int, total: Int) -> Unit)?
    ): SyncResult{
        val listNames = mutableListOf<String>()
        val choiceDtos = mutableListOf<ChoiceDto>()

        val response = try{
            choiceApiRepository.getListNames()
        }catch (e: Exception){
            return SyncResult.Error(e)
        }

        if(!response.isSuccessful){
            return response.toFailureResult()
        }

        val listNamesResponseBody = response.body()
            ?: return SyncResult.Error(IllegalStateException("Empty body"))

        listNames += listNamesResponseBody

        for(listName in listNames){

            val choicesResponse = try {
                choiceApiRepository.getChoicesByListName(listName = listName)
            }catch (e: Exception){
                return SyncResult.Error(e)
            }

            if(!choicesResponse.isSuccessful){
                return choicesResponse.toFailureResult()
            }

            val choicesResponseBody = choicesResponse.body()
                ?: return SyncResult.Error(IllegalStateException("Empty body"))

            choiceDtos += choicesResponseBody
        }

        val choices = choiceDtos.toEntities()

        database.withTransaction{
            choiceDao.insertAll(choices)
        }

        return SyncResult.Success(choices.size)
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
            ?: SyncResult.Error(IllegalStateException("Unknown server error: HTTP ${code()}"))
    }
}