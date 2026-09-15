package com.andrew.hdss.dtos

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse @OptIn(ExperimentalTime::class) constructor(
    val authenticationToken: String,
    val refreshToken: String,
    val firstName: String,
    val username: String,
    val role: String,
    val expiresAt: Instant
)
