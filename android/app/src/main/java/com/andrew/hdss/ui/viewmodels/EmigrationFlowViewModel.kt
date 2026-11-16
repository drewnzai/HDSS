package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.util.usecases.MembershipEndForms

class EmigrationFlowViewModel(
    private val householdClientId: String,
    private val emigrantClientId: String,
    formDao: FormDao,
    visitDao: VisitDao
) : FormFlowViewModel(
    formName = MembershipEndForms.EMIGRATION.formName,
    formDao = formDao,
    visitDao = visitDao,
    visitHouseholdClientId = householdClientId
) {
    override fun formContext() = FormFillContext(
        foreignKeys = mapOf("householdClientId" to householdClientId),
        presetAnswers = mapOf(MembershipEndForms.EMIGRATION.leavingQuestion to emigrantClientId)
    )

    companion object {
        fun factory(householdClientId: String, emigrantClientId: String) = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                EmigrationFlowViewModel(
                    householdClientId = householdClientId,
                    emigrantClientId = emigrantClientId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao()
                )
            }
        }
    }
}