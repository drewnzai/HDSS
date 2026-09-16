package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andrew.hdss.data.models.enums.LocationType

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("parentId")]
)
data class Location(
    @PrimaryKey
    val id: Long,
    val name: String,
    val type: LocationType,
    val parentId: Long?,
    val code: String?
)
