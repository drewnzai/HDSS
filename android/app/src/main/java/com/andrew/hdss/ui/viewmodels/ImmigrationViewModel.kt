package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.VisitDao
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.MembershipStartType
import java.util.UUID

class ImmigrationViewModel(
    private val householdClientId: String,
    formDao: FormDao,
    visitDao: VisitDao
) : FormFlowViewModel(
    formName = FORM_NAME,
    formDao = formDao,
    visitDao = visitDao,
    visitHouseholdClientId = householdClientId
) {

    private val individualClientId: String = UUID.randomUUID().toString()
    private val membershipClientId: String = UUID.randomUUID().toString()

    override fun formContext() = FormFillContext(
        entityClientIds = mapOf(
            MappedEntity.INDIVIDUAL to individualClientId,
            MappedEntity.MEMBERSHIP to membershipClientId
        ),
        foreignKeys = mapOf("householdClientId" to householdClientId),
        generatedFields = mapOf(
            MappedEntity.MEMBERSHIP to mapOf("startType" to MembershipStartType.IN_MIGRATION.name)
        )
    )

    companion object {
        const val FORM_NAME = "immigration-registration"

        fun factory(householdClientId: String) = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                ImmigrationViewModel(
                    householdClientId = householdClientId,
                    formDao = db.formDao(),
                    visitDao = db.visitDao()
                )
            }
        }
    }
}