package com.andrew.hdss.data.dtos

import com.andrew.hdss.data.models.Choice
import kotlinx.serialization.Serializable

@Serializable
data class ChoiceDto(
    val id: Long,
    val listName: String,
    val name: String,
    val label: String,
    val orderIndex: Int
)

fun List<ChoiceDto>.toEntities(): List<Choice>{
    return map {
        choiceDto ->
        Choice(
            id = choiceDto.id,
            listName = choiceDto.listName,
            name = choiceDto.name,
            label = choiceDto.label,
            orderIndex = choiceDto.orderIndex
        )
    }
}