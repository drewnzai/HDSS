package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.andrew.hdss.data.models.Membership
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(membership: Membership)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memberships: List<Membership>)

    @Update
    suspend fun update(membership: Membership)

    @Query("DELETE FROM memberships WHERE clientId = :clientId")
    suspend fun deleteByClientId(clientId: String)

    @Query("SELECT * FROM memberships WHERE clientId = :clientId")
    suspend fun getByClientId(clientId: String): Membership?

    @Query("SELECT * FROM memberships WHERE individualClientId = :individualClientId ORDER BY startDate")
    fun getByIndividual(individualClientId: String): Flow<List<Membership>>

    @Query("SELECT * FROM memberships WHERE householdClientId = :householdClientId ORDER BY startDate")
    fun getByHousehold(householdClientId: String): Flow<List<Membership>>

    @Query("""
        SELECT * FROM memberships
        WHERE individualClientId = :individualClientId AND endDate IS NULL
        LIMIT 1
    """)
    suspend fun getCurrentMembership(individualClientId: String): Membership?

    @Query("SELECT * FROM memberships WHERE householdClientId = :householdClientId AND endDate IS NULL")
    fun getCurrentMembers(householdClientId: String): Flow<List<Membership>>

    @Query("SELECT * FROM memberships WHERE synced = 0")
    suspend fun getUnsynced(): List<Membership>

    @Query("UPDATE memberships SET synced = 1, serverId = :serverId WHERE clientId = :clientId")
    suspend fun markSynced(clientId: String, serverId: Long)
}