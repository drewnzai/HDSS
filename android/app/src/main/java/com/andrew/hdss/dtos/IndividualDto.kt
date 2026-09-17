package com.andrew.hdss.dtos

import com.andrew.hdss.data.models.enums.Sex
import kotlinx.serialization.Serializable

@Serializable
data class IndividualDto(
    val id: Long,
    val clientId: String,
    val extendedId: String,
    val firstName: String,
    val lastName: String,
    val sex: Sex,
    val dateOfBirth: String,
    val dobEstimated: Boolean,
    val motherId: Long?,
    val fatherId: Long?
)
