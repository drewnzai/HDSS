package com.andrew.hdss.dtos

import com.andrew.hdss.data.models.enums.FormCategory
import com.andrew.hdss.data.models.enums.FormStatus
import com.andrew.hdss.data.models.enums.FormTarget
import kotlinx.serialization.Serializable

@Serializable
data class FormDto(
    val id: Long,
    val name: String,
    val title: String,
    val category: FormCategory,
    val target: FormTarget,
    val version: Int,
    val description: String,
    val active: Boolean,
    val status: FormStatus
)
