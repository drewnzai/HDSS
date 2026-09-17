package com.andrew.hdss.dtos

import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import kotlinx.serialization.Serializable

@Serializable
data class MembershipDto(
    val id: Long,
    val clientId: String,
    val individualId: Long,
    val householdId: Long,
    val relationshipToHead: RelationshipToHead,
    val startDate: String,
    val startType: MembershipStartType,
    val endDate: String?,
    val endType: MembershipEndType?
)
