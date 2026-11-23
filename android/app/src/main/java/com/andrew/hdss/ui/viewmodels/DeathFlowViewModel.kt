package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.util.usecases.MembershipEndForms

class DeathFlowViewModel(
    private val householdClientId: String,
    private val deceasedClientId: String,
    formDao: FormDao,
    visitDao: VisitDao
) : FormFlowViewModel(
    formSelector = FormSelector.ByName("death-registration"),
    formDao = formDao,
    visitDao = visitDao,
    visitHouseholdClientId = householdClientId
) {
    override fun formContext() = FormFillContext(
        foreignKeys = mapOf("householdClientId" to householdClientId),
        presetAnswers = mapOf(MembershipEndForms.DEATH.leavingQuestion to deceasedClientId)
    )

    companion object {
        fun factory(householdClientId: String, deceasedClientId: String) = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                DeathFlowViewModel(
                    householdClientId = householdClientId,
                    deceasedClientId = deceasedClientId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao()
                )
            }
        }
    }
}