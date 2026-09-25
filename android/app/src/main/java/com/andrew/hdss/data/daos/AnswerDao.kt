package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.andrew.hdss.data.models.Answer
import com.andrew.hdss.data.models.FormResponse

@Dao
interface AnswerDao {
    @Insert
    suspend fun insertAll(answers: List<Answer>)

    @Insert
    suspend fun insert(answer: Answer)

    @Query("SELECT * FROM answers WHERE synced = 0")
    suspend fun getUnsynced(): List<Answer>

    @Query("UPDATE answers SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}