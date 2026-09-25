package com.andrew.hdss.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.QuestionType

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = Form::class,
            parentColumns = ["id"],
            childColumns = ["formId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Question(
    @PrimaryKey
    val id: Long,
    val formId: Long,
    val name: String,
    val label: String,
    val hint: String?,
    val type: QuestionType,
    val required: Boolean,
    val relevant: String?,
    @ColumnInfo(name = "constraint_value")
    val constraint: String?,
    val constraintMessage: String?,
    val calculation: String?,
    val choiceListName: String?,
    val orderIndex: Int,
    val mappedEntity: MappedEntity = MappedEntity.NONE,
    val mappedField: String?
)
