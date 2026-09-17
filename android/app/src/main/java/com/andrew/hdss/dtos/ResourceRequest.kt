package com.andrew.hdss.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ResourceRequest(
    val page: Int = 0,
    val size: Int = 10
)
