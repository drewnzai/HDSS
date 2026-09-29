package com.andrew.hdss.data.dtos

import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import kotlinx.serialization.Serializable

@Serializable
data class MembershipPushDto(
    val clientId: String,
    val individualClientId: String,
    val householdClientId: String,
    val relationshipToHead: RelationshipToHead,
    val startDate: String,
    val startType: MembershipStartType,
    val endDate: String?,
    val endType: MembershipEndType?
)
