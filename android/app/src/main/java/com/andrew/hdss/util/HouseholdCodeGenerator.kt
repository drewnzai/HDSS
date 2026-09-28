package com.andrew.hdss.util

import com.andrew.hdss.data.daos.HouseholdDao

class HouseholdCodeGenerator(private val householdDao: HouseholdDao) {

    // ["Nairobi", "Embakasi", "Kayole"] -> "NEK-0000001", then "NEK-0000002".
    suspend fun next(pathNames: List<String>): String {
        require(pathNames.isNotEmpty()) { "A location path is required to build a household code" }

        val prefix = prefixFor(pathNames)
        val next = householdDao.getMaxCodeNumber(prefix) + 1
        return "$prefix-${next.toString().padStart(CODE_DIGITS, '0')}"
    }

    // First letter of each name, root to leaf. A name with no letters
    // contributes X, so the prefix always has one character per level and
    // can never contain a GLOB wildcard.
    private fun prefixFor(pathNames: List<String>): String =
        pathNames.joinToString("") { name ->
            (name.firstOrNull { it.isLetter() }?.uppercaseChar() ?: 'X').toString()
        }

    private companion object {
        const val CODE_DIGITS = 7
    }
}