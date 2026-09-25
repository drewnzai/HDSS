package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andrew.hdss.data.models.Form

@Dao
interface FormDao {

    @Query("SELECT * FROM forms ORDER BY name")
    suspend fun getAll(): List<Form>

    @Query("SELECT id FROM forms ORDER BY id ASC")
    suspend fun getAllIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(form: Form): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(forms: List<Form>)
}