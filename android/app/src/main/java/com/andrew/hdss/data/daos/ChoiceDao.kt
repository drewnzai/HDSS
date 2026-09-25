package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.andrew.hdss.data.models.Choice

@Dao
interface ChoiceDao {
    @Insert
    suspend fun insertAll(choices: List<Choice>)

    @Query("SELECT * FROM choices")
    suspend fun getAll(): List<Choice>
}