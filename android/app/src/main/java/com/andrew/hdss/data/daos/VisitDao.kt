package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT * FROM visits WHERE synced = 0")
    suspend fun getUnsynced(): List<Visit>

    @Query("UPDATE visits SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}