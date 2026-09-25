package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import java.time.LocalDate

@Entity(
    tableName = "memberships",
    foreignKeys = [
        ForeignKey(
            entity = Individual::class,
            parentColumns = ["clientId"],
            childColumns = ["individualClientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Household::class,
            parentColumns = ["clientId"],
            childColumns = ["householdClientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("individualClientId"), Index("householdClientId"), Index("serverId")]
)
data class Membership(
    @PrimaryKey
    val clientId: String,
    val serverId: Long?,
    val individualClientId: String,
    val householdClientId: String,
    val relationshipToHead: RelationshipToHead,
    val startDate: LocalDate,
    val startType: MembershipStartType,
    val endDate: LocalDate?,
    val endType: MembershipEndType?,
    val synced: Boolean = false
)