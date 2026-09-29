package com.andrew.hdss.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class SyncPushResponseDto(
    val households: List<SyncItemResultDto> = emptyList(),
    val individuals: List<SyncItemResultDto> = emptyList(),
    val memberships: List<SyncItemResultDto> = emptyList(),
    val visitResults: List<SyncItemResultDto> = emptyList(),
    val formResponseResults: List<SyncItemResultDto> = emptyList(),
    val answerResults: List<SyncItemResultDto> = emptyList()
)
