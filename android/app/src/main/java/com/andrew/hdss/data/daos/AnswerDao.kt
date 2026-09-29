package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.andrew.hdss.data.models.Answer

@Dao
interface AnswerDao {
    @Insert
    suspend fun insertAll(answers: List<Answer>)

    @Insert
    suspend fun insert(answer: Answer)

    @Query("SELECT * FROM answers WHERE synced = 0")
    suspend fun getUnsynced(): List<Answer>

    @Query("SELECT * FROM answers WHERE formResponseId IN (:formResponseIds)")
    suspend fun getByFormResponseIds(formResponseIds: List<String>): List<Answer>

    @Query("UPDATE answers SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)
}