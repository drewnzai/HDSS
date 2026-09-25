package com.andrew.hdss.data.dtos

import com.andrew.hdss.data.models.enums.FormResponseStatus
import kotlinx.serialization.Serializable

@Serializable
data class FormResponsePushDto(
    val clientId: String,
    val visitClientId: String,
    val formId: Long,
    val formVersion: Int,
    val status: FormResponseStatus,
    val startedAt: String,
    val completedAt: String
)
