package com.andrew.hdss.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ResourceRequest(
    val page: Int = 0,
    val size: Int = 10
)

fun ResourceRequest.toQueryMap(): Map<String, String> = mapOf(
    "page" to page.toString(),
    "size" to size.toString()
)
