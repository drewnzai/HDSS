package com.andrew.hdss.data.dtos

import com.andrew.hdss.data.models.Question
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.QuestionType
import kotlinx.serialization.Serializable

@Serializable
data class QuestionDto(
    val id: Long,
    val formId: Long,
    val name: String,
    val label: String,
    val hint: String,
    val type: QuestionType,
    val required: Boolean,
    val relevant: String,
    val constraint: String,
    val constraintMessage: String,
    val calculation: String,
    val choiceListName: String,
    val orderIndex: Int,
    val mappedEntity: MappedEntity,
    val mappedField: String
)

fun List<QuestionDto>.toEntities(): List<Question>{
    return map{
        questionDto ->
        Question(
            id = questionDto.id,
            formId = questionDto.formId,
            name = questionDto.name,
            label = questionDto.label,
            hint = questionDto.hint,
            type = questionDto.type,
            required = questionDto.required,
            relevant = questionDto.relevant,
            constraint = questionDto.constraint,
            constraintMessage = questionDto.constraintMessage,
            calculation = questionDto.calculation,
            choiceListName = questionDto.choiceListName,
            orderIndex = questionDto.orderIndex,
            mappedEntity = questionDto.mappedEntity,
            mappedField = questionDto.mappedField
        )
    }
}