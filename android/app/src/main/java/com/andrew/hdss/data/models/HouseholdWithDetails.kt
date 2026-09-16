package com.andrew.hdss.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class HouseholdWithDetails(
    @Embedded
    val household: Household,

    @Relation(
        parentColumn = "locationId",
        entityColumn = "id"
    )
    val location: Location?,

    @Relation(
        parentColumn = "headIndividualClientId",
        entityColumn = "clientId"
    )
    val head: Individual?
) {
    val locationName: String? get() = location?.name
    val headName: String? get() = head?.let { "${it.firstName} ${it.lastName}" }
}