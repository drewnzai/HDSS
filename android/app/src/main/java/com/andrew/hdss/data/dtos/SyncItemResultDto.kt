package com.andrew.hdss.data.dtos

import kotlinx.serialization.Serializable

@Serializable
enum class SyncStatus { CREATED, UPDATED, ERROR }

@Serializable
data class SyncItemResultDto(
    val clientId: String,
    val id: Long? = null,
    val status: SyncStatus,
    val message: String? = null
) {
    val isSuccess: Boolean get() = status == SyncStatus.CREATED || status == SyncStatus.UPDATED
}