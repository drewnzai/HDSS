package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andrew.hdss.data.models.Location
import com.andrew.hdss.data.models.enums.LocationType
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<Location>)

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getById(id: Long): Location?

    @Query("SELECT * FROM locations WHERE id = :id")
    fun observeById(id: Long): Flow<Location?>

    @Query("SELECT * FROM locations WHERE parentId IS :parentId")
    fun getChildren(parentId: Long?): Flow<List<Location>>

    @Query("SELECT * FROM locations WHERE type = :type")
    fun getByType(type: LocationType): Flow<List<Location>>

    @Query("SELECT * FROM locations")
    fun getAll(): Flow<List<Location>>

    @Query("SELECT count(*) FROM locations")
    suspend fun getCount(): Long

    @Query("DELETE FROM locations")
    suspend fun deleteAll()

    @Query("SELECT id FROM locations")
    suspend fun getAllIds(): List<Long>
}