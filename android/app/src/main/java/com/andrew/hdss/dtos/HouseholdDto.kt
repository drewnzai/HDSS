package com.andrew.hdss.dtos

import com.andrew.hdss.data.models.enums.HouseholdStatus
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdDto(
    val id: Long,
    val clientId: String,
    val householdCode: String,
    val locationId: Long?,
    val locationName: String?,
    val latitude: Double,
    val longitude: Double,
    val headIndividualId: Long?,
    val headName: String?,
    val status: HouseholdStatus
)

