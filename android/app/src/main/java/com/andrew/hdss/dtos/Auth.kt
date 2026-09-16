package com.andrew.hdss.dtos

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse (
    val authenticationToken: String,
    val refreshToken: String,
    val firstName: String,
    val username: String,
    val role: String,
    val expiresAt: String
)
