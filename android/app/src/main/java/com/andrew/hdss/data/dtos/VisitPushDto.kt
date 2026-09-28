package com.andrew.hdss.data.dtos

import com.andrew.hdss.data.models.enums.VisitStatus
import kotlinx.serialization.Serializable

@Serializable
data class VisitPushDto(
    val clientId: String,
    val householdClientId: String,
    val individualClientId: String,
    val visitDate: String,
    val status: VisitStatus
)
