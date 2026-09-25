package com.andrew.hdss.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.utils.IndividualIdentifiers
import kotlinx.coroutines.flow.Flow

@Dao
interface IndividualDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(individual: Individual)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(individuals: List<Individual>)

    @Update
    suspend fun update(individual: Individual)

    @Query("DELETE FROM individuals WHERE clientId = :clientId")
    suspend fun deleteByClientId(clientId: String)

    @Query("SELECT * FROM individuals WHERE clientId = :clientId")
    suspend fun getByClientId(clientId: String): Individual?

    @Query("SELECT * FROM individuals WHERE clientId = :clientId")
    fun observeByClientId(clientId: String): Flow<Individual?>

    @Query("SELECT * FROM individuals WHERE clientId IN (:clientIds)")
    suspend fun getByClientIds(clientIds: List<String>): List<Individual>

    @Query("SELECT * FROM individuals ORDER BY firstName, lastName")
    fun getAll(): Flow<List<Individual>>

    @Query("""
        SELECT * FROM individuals
        WHERE firstName LIKE '%' || :query || '%'
           OR lastName LIKE '%' || :query || '%'
           OR extendedId LIKE '%' || :query || '%'
        ORDER BY firstName, lastName
    """)
    fun search(query: String): Flow<List<Individual>>

    @Query("SELECT clientId, serverId FROM individuals WHERE serverId IN (:serverIds)")
    suspend fun getIdentifiersByServerIds(serverIds: List<Long>): List<IndividualIdentifiers>

    @Query("SELECT * FROM individuals WHERE synced = 0")
    suspend fun getUnsynced(): List<Individual>

    @Query("UPDATE individuals SET synced = 1, serverId = :serverId WHERE clientId = :clientId")
    suspend fun markSynced(clientId: String, serverId: Long)
}