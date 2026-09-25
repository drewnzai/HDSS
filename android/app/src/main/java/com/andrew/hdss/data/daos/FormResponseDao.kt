package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.andrew.hdss.data.models.FormResponse

@Dao
interface FormResponseDao {
    @Insert
    suspend fun insert(formResponseDao: FormResponseDao)

    @Query("SELECT * FROM form_responses WHERE synced = 0")
    suspend fun getUnsynced(): List<FormResponse>

    @Query("UPDATE form_responses SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}