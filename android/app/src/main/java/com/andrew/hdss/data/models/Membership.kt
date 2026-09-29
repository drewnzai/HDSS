package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andrew.hdss.data.dtos.MembershipPushDto
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

fun List<Membership>.toPushDtos(): List<MembershipPushDto> = map {
    MembershipPushDto(
        clientId = it.clientId,
        individualClientId = it.individualClientId,
        householdClientId = it.householdClientId,
        relationshipToHead = it.relationshipToHead,
        startDate = it.startDate.toString(),
        startType = it.startType,
        endDate = it.endDate?.toString(),
        endType = it.endType?.let { type -> type }
    )
}