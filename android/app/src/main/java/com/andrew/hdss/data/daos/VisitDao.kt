package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.andrew.hdss.data.models.Visit

@Dao
interface VisitDao {
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insert(visit: Visit);

    @Update
    suspend fun update(visit: Visit)
}