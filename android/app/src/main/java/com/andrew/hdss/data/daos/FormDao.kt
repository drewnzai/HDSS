package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.andrew.hdss.data.models.Form
import com.andrew.hdss.data.models.relations.FormWithQuestions

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

    @Transaction
    @Query("SELECT * FROM forms WHERE id = :formId")
    suspend fun getFormWithQuestions(formId: Long): FormWithQuestions?
}