package com.andrew.hdss.dtos

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class ErrorResponse(
    val title: String,
    val status: Int,
    val detail: String,
    val path: String,
    val timestamp: Instant
)
