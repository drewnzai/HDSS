package com.andrew.hdss.data.dtos

import android.util.Log
import com.andrew.hdss.data.models.Location
import com.andrew.hdss.data.models.enums.LocationType
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: Long,
    val name: String,
    val type: String,
    val parentId: Long?,
    val code: String?
)

fun LocationDto.toEntity(): Location? {
    val locationType = try {
        LocationType.valueOf(type)
    } catch (e: IllegalArgumentException) {
        Log.w("LocationMapper", "Unknown LocationType '$type' for location id=$id — skipping")
        return null
    }

    return Location(
        id = id,
        name = name,
        type = locationType,
        parentId = parentId,
        code = code
    )
}