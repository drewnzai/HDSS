package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.andrew.hdss.data.models.enums.Sex
import java.time.LocalDate

@Entity(
    tableName = "individuals",
    foreignKeys = [
        ForeignKey(
            entity = Individual::class,
            parentColumns = ["clientId"],
            childColumns = ["motherClientId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Individual::class,
            parentColumns = ["clientId"],
            childColumns = ["fatherClientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("motherClientId"), Index("fatherClientId"), Index("serverId")]
)
data class Individual(
    @PrimaryKey
    val clientId: String,
    val serverId: Long?,
    val extendedId: String?,
    val firstName: String,
    val lastName: String,
    val sex: Sex,
    val dateOfBirth: LocalDate?,
    val dobEstimated: Boolean,
    val motherClientId: String?,
    val fatherClientId: String?
)