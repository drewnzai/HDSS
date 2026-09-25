package com.andrew.hdss.data.dtos

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

fun List<IndividualDto>.topologicallySorted(): List<IndividualDto> {
    val byId = associateBy { it.id }
    val result = mutableListOf<IndividualDto>()
    val visited = mutableSetOf<Long>()
    val visiting = mutableSetOf<Long>()

    fun visit(id: Long) {
        if (id in visited) return

        // Detect malformed/cyclic data
        if (!visiting.add(id)) {
            throw IllegalStateException(
                "Circular parent relationship detected involving individual id=$id"
            )
        }

        val individual = byId[id]
        if (individual == null) {
            // Referenced as a parent but not present in this batch — not an
            // error. resolve() in toEntities() already handles this id being
            // absent by nulling the reference and logging a warning.
            visiting.remove(id)
            visited.add(id)
            return
        }

        // Parents must come first
        individual.motherId?.let { visit(it) }
        individual.fatherId?.let { visit(it) }

        visiting.remove(id)
        visited.add(id)
        result.add(individual)
    }

    forEach { visit(it.id) }

    return result
}

fun List<IndividualDto>.toEntities(): List<Individual> {
    val sorted = topologicallySorted()

    val idToClientId = sorted.associate { it.id to it.clientId }

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

    return sorted.map { dto ->
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