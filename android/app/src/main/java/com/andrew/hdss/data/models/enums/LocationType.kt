package com.andrew.hdss.data.models.enums

enum class LocationType(val level:Int) {
    COUNTRY(0),
    COUNTY(1),
    SUB_COUNTY(2),
    LOCATION(3),
    SUB_LOCATION(4);

    fun parentType(): LocationType? =
        if (level == 0) null else entries.first { it.level == level - 1 }
}
