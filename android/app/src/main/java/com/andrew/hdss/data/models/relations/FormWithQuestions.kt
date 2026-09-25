package com.andrew.hdss.data.models.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.andrew.hdss.data.models.Form
import com.andrew.hdss.data.models.Question

data class FormWithQuestions(
    @Embedded
    val form: Form,

    @Relation(
        parentColumn = "id",
        entityColumn = "formId"
    )
    val questions: List<Question>
)
