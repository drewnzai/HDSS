package com.andrew.hdss.data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class SyncPushRequestDto(
    val households: List<HouseholdPushDto>,
    val individuals: List<IndividualPushDto>,
    val memberships: List<MembershipPushDto>,
    val visits: List<VisitPushDto>,
    val formResponses: List<FormResponsePushDto>,
    val answers: List<AnswerPushDto>
)
