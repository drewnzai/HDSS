package com.andrew.hdss.dtos

import android.util.Log
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.models.enums.Sex
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class IndividualDto(
    val id: Long,
    val clientId: String,
    val extendedId: String,
    val firstName: String,
    val lastName: String,
    val sex: Sex,
    val dateOfBirth: String,
    val dobEstimated: Boolean,
    val motherId: Long?,
    val fatherId: Long?
)

fun List<IndividualDto>.toEntities(): List<Individual> {
    val idToClientId: Map<Long, String> = associate { it.id to it.clientId }

    fun resolve(parentId: Long?, role: String, ownClientId: String): String? {
        if (parentId == null) return null
        return idToClientId[parentId] ?: run {
            Log.w(
                "IndividualMapper",
                "$role id=$parentId for individual $ownClientId not found in this batch — dropping reference"
            )
            null
        }
    }

    return map { dto ->
        Individual(
            clientId = dto.clientId,
            serverId = dto.id,
            extendedId = dto.extendedId,
            firstName = dto.firstName,
            lastName = dto.lastName,
            sex = dto.sex,
            dateOfBirth = LocalDate.parse(dto.dateOfBirth),
            dobEstimated = dto.dobEstimated,
            motherClientId = resolve(dto.motherId, "motherId", dto.clientId),
            fatherClientId = resolve(dto.fatherId, "fatherId", dto.clientId)
        )
    }
}
