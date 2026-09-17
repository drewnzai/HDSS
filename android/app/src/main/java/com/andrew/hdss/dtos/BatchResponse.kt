package com.andrew.hdss.dtos

import kotlinx.serialization.Serializable

@Serializable
data class BatchResponse<T>(
val size: Int? = null,
val page: Int? = null,
val totalElements: Long? = null,
val totalPages: Int? = null,
val data: MutableList<T> = mutableListOf()
)
