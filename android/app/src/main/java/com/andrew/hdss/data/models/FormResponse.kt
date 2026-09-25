package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.andrew.hdss.data.dtos.FormResponsePushDto
import com.andrew.hdss.data.models.enums.FormResponseStatus
import java.time.LocalDateTime

@Entity(
    tableName = "form_responses",
    foreignKeys = [
        ForeignKey(
            entity = Visit::class,
            parentColumns = ["id"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Form::class,
            parentColumns = ["id"],
            childColumns = ["formId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FormResponse(
    @PrimaryKey
    val id: String,
    val visitId: String,
    val formId: Long,
    val formVersion: Int,
    val status: FormResponseStatus,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime,
    val synced: Boolean = false
)

fun List<FormResponse>.toPushDtos(): List<FormResponsePushDto>{
    return map{
        formResponse ->
        FormResponsePushDto(
            clientId = formResponse.id,
            visitClientId = formResponse.visitId,
            formId = formResponse.formId,
            formVersion = formResponse.formVersion,
            status = formResponse.status,
            startedAt = formResponse.startedAt.toString(),
            completedAt = formResponse.completedAt.toString()
        )
    }
}