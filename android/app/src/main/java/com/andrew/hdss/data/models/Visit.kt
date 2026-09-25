package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.andrew.hdss.data.models.enums.VisitStatus
import com.andrew.hdss.data.dtos.VisitPushDto
import java.time.LocalDateTime

@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["clientId"],
            childColumns = ["householdClientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Individual::class,
            parentColumns = ["clientId"],
            childColumns = ["individualClientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Visit(
    @PrimaryKey
    val id: String,
    val householdClientId: String,
    val individualClientId: String,
    val visitDate: LocalDateTime?,
    val status: VisitStatus,
    val synced: Boolean = false
)

fun List<Visit>.toPushDtos(): List<VisitPushDto>{
    return map { visit ->
        VisitPushDto(
            clientId = visit.id,
            householdClientId = visit.householdClientId,
            individualClientId = visit.individualClientId,
            visitDate = visit.visitDate.toString(),
            status = visit.status
        )
    }
}