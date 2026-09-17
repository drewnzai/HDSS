package com.andrew.hdss.dtos

import android.util.Log
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

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

suspend fun List<MembershipDto>.toEntities(
    individualDao: IndividualDao,
    householdDao: HouseholdDao
): List<Membership> {
    val individualServerIds = map { it.individualId }.distinct()
    val householdServerIds = map { it.householdId }.distinct()

    val individualClientIdByServerId: Map<Long, String> =
        if (individualServerIds.isEmpty()) {
            emptyMap()
        } else {
            individualDao.getIdentifiersByServerIds(individualServerIds)
                .mapNotNull { identifiers -> identifiers.serverId?.let { it to identifiers.clientId } }
                .toMap()
        }

    val householdClientIdByServerId: Map<Long, String> =
        if (householdServerIds.isEmpty()) {
            emptyMap()
        } else {
            householdDao.getIdentifiersByServerIds(householdServerIds)
                .mapNotNull { identifiers -> identifiers.serverId?.let { it to identifiers.clientId } }
                .toMap()
        }

    return mapNotNull { dto ->
        val individualClientId = individualClientIdByServerId[dto.individualId] ?: run {
            Log.w(
                "MembershipMapper",
                "individualId=${dto.individualId} for membership ${dto.clientId} not found locally — skipping membership"
            )
            return@mapNotNull null
        }

        val householdClientId = householdClientIdByServerId[dto.householdId] ?: run {
            Log.w(
                "MembershipMapper",
                "householdId=${dto.householdId} for membership ${dto.clientId} not found locally — skipping membership"
            )
            return@mapNotNull null
        }

        Membership(
            clientId = dto.clientId,
            serverId = dto.id,
            individualClientId = individualClientId,
            householdClientId = householdClientId,
            relationshipToHead = dto.relationshipToHead,
            startDate = LocalDate.parse(dto.startDate),
            startType = dto.startType,
            endDate = dto.endDate?.let { LocalDate.parse(it) },
            endType = dto.endType
        )
    }
}
