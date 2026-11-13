package com.andrew.hdss.ui.labels

import com.andrew.hdss.data.models.enums.HouseholdStatus

fun HouseholdStatus.displayLabel(): String = when (this) {
    HouseholdStatus.ACTIVE -> "Active"
    HouseholdStatus.NOT_VIABLE -> "Not viable"
    else -> name
}