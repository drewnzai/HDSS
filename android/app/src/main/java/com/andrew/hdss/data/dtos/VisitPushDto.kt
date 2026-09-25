package com.andrew.hdss.data.dtos

import com.andrew.hdss.data.models.enums.VisitStatus
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class VisitPushDto(
    val clientId: String,
    val householdClientId: String,
    val visitDate: String,
    val status: VisitStatus
)
