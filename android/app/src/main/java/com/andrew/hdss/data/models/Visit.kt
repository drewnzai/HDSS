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
        )
    ]
)
data class Visit(
    @PrimaryKey
    val clientId: String,
    val householdClientId: Long,
    val visitDate: LocalDateTime?,
    val status: VisitStatus
)

fun List<Visit>.toPushDtos(): List<VisitPushDto>{
    return map { visit ->
        VisitPushDto(
            clientId = visit.clientId,
            householdClientId = visit.householdClientId.toString(),
            visitDate = visit.visitDate.toString(),
            status = visit.status
        )
    }
}