package com.andrew.hdss.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ColumnInfo
import com.andrew.hdss.data.models.enums.FormCategory
import com.andrew.hdss.data.models.enums.FormStatus
import com.andrew.hdss.data.models.enums.FormTarget
import java.time.LocalDateTime

@Entity(
    tableName = "forms"
)
data class Form(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "category")
    val category: FormCategory,

    @ColumnInfo(name = "target")
    val target: FormTarget,

    @ColumnInfo(name = "version")
    val version: Int,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "active")
    val active: Boolean,

    @ColumnInfo(name = "status")
    val status: FormStatus,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime? = null
)