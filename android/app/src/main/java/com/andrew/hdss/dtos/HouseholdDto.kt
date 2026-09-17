package com.andrew.hdss.dtos

import android.util.Log
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.daos.LocationDao
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.data.models.enums.HouseholdStatus
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdDto(
    val id: Long,
    val clientId: String,
    val householdCode: String,
    val locationId: Long?,
    val locationName: String?,
    val latitude: Double,
    val longitude: Double,
    val headIndividualId: Long?,
    val headName: String?,
    val status: HouseholdStatus
)

suspend fun List<HouseholdDto>.toEntities(
    individualDao: IndividualDao,
    locationDao: LocationDao
): List<Household> {
    val headServerIds = mapNotNull { it.headIndividualId }.distinct()
    val headClientIdByServerId: Map<Long, String> =
        if (headServerIds.isEmpty()) {
            emptyMap()
        } else {
            individualDao.getIdentifiersByServerIds(headServerIds)
                .mapNotNull { identifiers ->
                    identifiers.serverId?.let { it to identifiers.clientId }
                }
                .toMap()
        }

    val validLocationIds: Set<Long> = locationDao.getAllIds().toSet()

    return map { dto ->
        val headIndividualClientId = dto.headIndividualId?.let { headId ->
            headClientIdByServerId[headId] ?: run {
                Log.w(
                    "HouseholdMapper",
                    "headIndividualId=$headId for household ${dto.clientId} not found locally — dropping reference"
                )
                null
            }
        }

        val locationId = dto.locationId?.let { locId ->
            if (locId in validLocationIds) {
                locId
            } else {
                Log.w(
                    "HouseholdMapper",
                    "locationId=$locId for household ${dto.clientId} not found locally — dropping reference"
                )
                null
            }
        }

        Household(
            clientId = dto.clientId,
            serverId = dto.id,
            householdCode = dto.householdCode,
            locationId = locationId,
            latitude = dto.latitude,
            longitude = dto.longitude,
            headIndividualClientId = headIndividualClientId,
            status = dto.status
        )
    }
}

