package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao

class ExtraFormFlowViewModel(
    formId: Long,
    private val householdClientId: String,
    private val personClientId: String?,
    formDao: FormDao,
    visitDao: VisitDao
) : FormFlowViewModel(
    formSelector = FormSelector.ById(formId),
    formDao = formDao,
    visitDao = visitDao,
    visitHouseholdClientId = householdClientId,
    visitIndividualClientId = personClientId
) {
    override fun formContext() = FormFillContext(
        foreignKeys = buildMap {
            put("householdClientId", householdClientId)
            personClientId?.let { put("personClientId", it) }
        }
    )

    companion object {
        fun factory(formId: Long, householdClientId: String, personClientId: String?) =
            viewModelFactory {
                initializer {
                    val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                    ExtraFormFlowViewModel(
                        formId = formId,
                        householdClientId = householdClientId,
                        personClientId = personClientId,
                        formDao = db.formDao(),
                        visitDao = db.visitDao()
                    )
                }
            }
    }
}