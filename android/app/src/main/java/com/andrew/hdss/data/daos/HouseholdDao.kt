package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.data.models.HouseholdWithDetails
import com.andrew.hdss.data.utils.HouseholdIdentifiers
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(household: Household)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(households: List<Household>)

    @Update
    suspend fun update(household: Household)

    @Query("DELETE FROM households WHERE clientId = :clientId")
    suspend fun deleteByClientId(clientId: String)

    @Query("SELECT * FROM households WHERE clientId = :clientId")
    suspend fun getByClientId(clientId: String): Household?

    @Query("SELECT * FROM households WHERE clientId = :clientId")
    fun observeByClientId(clientId: String): Flow<Household?>

    @Query("SELECT * FROM households WHERE locationId = :locationId")
    fun getByLocationId(locationId: Long): Flow<List<Household>>

    @Query("SELECT * FROM households")
    fun getAll(): Flow<List<Household>>

    @Transaction
    @Query("SELECT * FROM households WHERE clientId = :clientId")
    fun observeWithDetails(clientId: String): Flow<HouseholdWithDetails?>

    @Transaction
    @Query("SELECT * FROM households WHERE locationId = :locationId")
    fun getWithDetailsByLocation(locationId: Long): Flow<List<HouseholdWithDetails>>

    @Transaction
    @Query("SELECT * FROM households")
    fun getAllWithDetails(): Flow<List<HouseholdWithDetails>>

    @Query("SELECT clientId, serverId FROM households WHERE serverId IN (:serverIds)")
    suspend fun getIdentifiersByServerIds(serverIds: List<Long>): List<HouseholdIdentifiers>

    @Query("SELECT * FROM households WHERE synced = 0")
    suspend fun getUnsynced(): List<Household>

    @Query("UPDATE households SET synced = 1, serverId = :serverId WHERE clientId = :clientId")
    suspend fun markSynced(clientId: String, serverId: Long)
}