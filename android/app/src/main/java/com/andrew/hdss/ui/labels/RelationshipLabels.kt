package com.andrew.hdss.ui.labels

import com.andrew.hdss.data.models.enums.RelationshipToHead

fun RelationshipToHead.displayLabel(): String = when (this) {
    RelationshipToHead.HEAD -> "Head"
    RelationshipToHead.SPOUSE -> "Spouse"
    RelationshipToHead.CHILD -> "Child"
    RelationshipToHead.PARENT -> "Parent"
    RelationshipToHead.SIBLING -> "Sibling"
    RelationshipToHead.OTHER_RELATIVE -> "Other relative"
    RelationshipToHead.NON_RELATIVE -> "Non-relative"
    else -> name
}