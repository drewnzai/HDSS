package com.andrew.hdss.util.usecases

import com.andrew.hdss.data.models.enums.MembershipEndType

data class MembershipEndSpec(
    val formName: String,
    val leavingQuestion: String,
    val dateQuestion: String,
    val endType: MembershipEndType,
    val informantQuestion: String? = null
)

object MembershipEndForms {
    val DEATH = MembershipEndSpec(
        formName = "death-registration",
        leavingQuestion = "deceased_clientId",
        dateQuestion = "date_of_death",
        endType = MembershipEndType.DEATH,
        informantQuestion = "informant_clientId"
    )

    val EMIGRATION = MembershipEndSpec(
        formName = "emigration-registration",
        leavingQuestion = "emigrant_clientId",
        dateQuestion = "emigration_date",
        endType = MembershipEndType.EMIGRATION
    )

    private val all = listOf(DEATH, EMIGRATION)

    val leavingQuestionNames: Set<String> = all.map { it.leavingQuestion }.toSet()
    val informantQuestionNames: Set<String> = all.mapNotNull { it.informantQuestion }.toSet()

    fun forForm(formName: String): MembershipEndSpec? =
        all.firstOrNull { it.formName == formName }
}