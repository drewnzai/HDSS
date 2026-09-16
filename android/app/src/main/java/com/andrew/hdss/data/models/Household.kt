package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andrew.hdss.data.models.enums.HouseholdStatus

@Entity(
    tableName = "households",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Individual::class,
            parentColumns = ["clientId"],
            childColumns = ["headIndividualClientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("locationId"), Index("headIndividualClientId"), Index("serverId")]
)
data class Household(
    @PrimaryKey
    val clientId: String,
    val serverId: Long?,
    val householdCode: String?,
    val locationId: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val headIndividualClientId: String?,
    val status: HouseholdStatus
)