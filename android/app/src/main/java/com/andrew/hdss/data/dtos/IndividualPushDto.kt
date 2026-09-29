package com.andrew.hdss.data.dtos

import com.andrew.hdss.data.models.enums.Sex
import kotlinx.serialization.Serializable

@Serializable
data class IndividualPushDto(
    val clientId: String,
    val extendedId: String?,
    val firstName: String,
    val lastName: String,
    val sex: Sex,
    val dateOfBirth: String?,
    val dobEstimated: Boolean,
    val motherClientId: String?,
    val fatherClientId: String?
)
