package com.andrew.hdss.util.usecases

import com.andrew.hdss.data.models.enums.MembershipEndType

data class MembershipEndSpec(
    val formName: String,
    val leavingQuestion: String,
    val dateQuestion: String,
    val endType: MembershipEndType
)

object MembershipEndForms {
    val DEATH = MembershipEndSpec(
        formName = "death-registration",
        leavingQuestion = "deceased_clientId",
        dateQuestion = "date_of_death",
        endType = MembershipEndType.DEATH
    )

    val EMIGRATION = MembershipEndSpec(
        formName = "emigration-registration",
        leavingQuestion = "emigrant_clientId",
        dateQuestion = "emigration_date",
        endType = MembershipEndType.EMIGRATION
    )

    private val all = listOf(DEATH, EMIGRATION)

    val leavingQuestionNames: Set<String> = all.map { it.leavingQuestion }.toSet()

    fun forForm(formName: String): MembershipEndSpec? =
        all.firstOrNull { it.formName == formName }
}