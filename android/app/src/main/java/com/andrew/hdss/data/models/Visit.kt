package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.andrew.hdss.data.models.enums.VisitStatus
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
    val serverId: Long?,
    val householdClientId: Long?,
    val visitDate: LocalDateTime?,
    val status: VisitStatus?
) {
}