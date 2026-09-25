package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "choices"
)
data class Choice(
    @PrimaryKey
    val id: Long,
    val listName: String,
    val name: String,
    val label: String,
    val orderIndex: Int
)
