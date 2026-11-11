package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.enums.RelationshipToHead
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class MemberRow(
    val clientId: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate?,
    val relationshipToHead: RelationshipToHead
)
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
    SELECT i.clientId AS clientId,
           i.firstName AS firstName,
           i.lastName AS lastName,
           i.dateOfBirth AS dateOfBirth,
           m.relationshipToHead AS relationshipToHead
    FROM memberships m
    INNER JOIN individuals i ON i.clientId = m.individualClientId
    WHERE m.householdClientId = :householdClientId
      AND m.endDate IS NULL
    ORDER BY CASE WHEN m.relationshipToHead = 'HEAD' THEN 0 ELSE 1 END, i.firstName
""")
    fun observeOpenMembers(householdClientId: String): Flow<List<MemberRow>>

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