package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.data.models.enums.MappedEntity
import java.util.UUID

class AddIndividualViewModel(
    private val householdClientId: String,
    formDao: FormDao,
    visitDao: VisitDao
) : FormFlowViewModel(
    formName = "individual-registration",
    formDao = formDao,
    visitDao = visitDao,
    visitHouseholdClientId = householdClientId
) {
    private val individualClientId = UUID.randomUUID().toString()
    private val membershipClientId = UUID.randomUUID().toString()

    override fun formContext() = FormFillContext(
        entityClientIds = mapOf(
            MappedEntity.INDIVIDUAL to individualClientId,
            MappedEntity.MEMBERSHIP to membershipClientId
        ),
        foreignKeys = mapOf("householdClientId" to householdClientId)
    )

    companion object {
        fun factory(householdClientId: String) = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                AddIndividualViewModel(
                    householdClientId = householdClientId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao()
                )
            }
        }
    }
}