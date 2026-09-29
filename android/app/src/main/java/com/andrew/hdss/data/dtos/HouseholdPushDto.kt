package com.andrew.hdss.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class HouseholdPushDto(
    val clientId: String,
    val householdCode: String?,
    val locationId: Long?,
    val latitude: Double?,
    val longitude: Double?
)
