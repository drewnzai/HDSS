package com.andrew.hdss.network.services

import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.QuestionDao
import com.andrew.hdss.data.dtos.ErrorResponse
import com.andrew.hdss.data.dtos.QuestionDto
import com.andrew.hdss.data.dtos.toEntities
import com.andrew.hdss.data.models.Question
import com.andrew.hdss.network.SyncResult
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface QuestionApiRepository{
    @GET("forms/{formId}/questions")
    suspend fun getAllQuestions(
        @Path("formId") formId: Long
    ): Response<List<QuestionDto>>
}

class QuestionApiService(
    private val questionApiRepository: QuestionApiRepository,
    private val formDao: FormDao,
    private val questionDao: QuestionDao,
    private val database: AppDatabase,
    private val json: Json
) {

    suspend fun getQuestionsByFormId(
        onProgress: (suspend (Int, Int) -> Unit)? = null
    ): SyncResult {
        val allDtos = mutableListOf<QuestionDto>()
        val forms = formDao.getAllIds()

        if(forms == emptyList<Long>()){
            return SyncResult.NetworkError(RuntimeException("No forms stored in the database"))
        }

        for(formId in forms){
            val response = try{
                questionApiRepository.getAllQuestions(formId)
            }catch(e: Exception){
                return SyncResult.NetworkError(e)
            }

            if(!response.isSuccessful){
                return response.toFailureResult()
            }

            val dtos = response.body()
                ?: return SyncResult.NetworkError(IllegalStateException("Empty response body"))

            allDtos += dtos
        }

        val questions: List<Question> = allDtos.toEntities()

        database.withTransaction {
            questionDao.insertAll(questions)
        }

        return SyncResult.Success(questions.size)
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
            ?: SyncResult.NetworkError(IllegalStateException("Unknown server error: HTTP ${code()}"))
    }
}