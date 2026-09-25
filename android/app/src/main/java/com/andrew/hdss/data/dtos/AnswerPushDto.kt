package com.andrew.hdss.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AnswerPushDto(
    val clientId: String,
    val formResponseClientId: String,
    val questionId: Long,
    val value: String
)
