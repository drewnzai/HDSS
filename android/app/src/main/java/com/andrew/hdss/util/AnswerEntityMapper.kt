package com.andrew.hdss.util


import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.Question
import com.andrew.hdss.data.models.enums.HouseholdStatus
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import com.andrew.hdss.data.models.enums.Sex
import java.time.LocalDate

object AnswerEntityMapper {

    fun buildFieldBags(
        visibleQuestions: List<Question>,
        answers: Map<Long, String>
    ): Map<MappedEntity, MutableMap<String, String>> {
        val bags = mutableMapOf<MappedEntity, MutableMap<String, String>>()
        for (q in visibleQuestions) {
            if (q.mappedEntity == MappedEntity.NONE) continue
            val value = answers[q.id] ?: continue // mutual-exclusivity guard: skip unanswered/non-relevant
            val fieldNames = q.mappedField?.split(",")?.map { it.trim() } ?: continue
            val bag = bags.getOrPut(q.mappedEntity) { mutableMapOf() }
            if (fieldNames.size == 2) {
                // GEOPOINT convention: captured value is ODK's own
                // space-separated "lat lon alt acc" format.
                val parts = value.split(" ")
                bag[fieldNames[0]] = parts.getOrElse(0) { "" }
                bag[fieldNames[1]] = parts.getOrElse(1) { "" }
            } else {
                bag[fieldNames[0]] = value
            }
        }
        return bags
    }

    fun buildIndividual(clientId: String, fields: Map<String, String>): Individual = Individual(
        clientId = clientId,
        serverId = null,
        extendedId = null,
        firstName = fields.getValue("firstName"),
        lastName = fields.getValue("lastName"),
        sex = Sex.valueOf(fields.getValue("sex")),
        dateOfBirth = fields["dateOfBirth"]?.let { LocalDate.parse(it) },
        dobEstimated = fields["dobEstimated"]?.toBooleanStrictOrNull() ?: false,
        motherClientId = fields["motherClientId"]?.takeIf { it.isNotBlank() },
        fatherClientId = fields["fatherClientId"]?.takeIf { it.isNotBlank() },
        synced = false
    )

    // headIndividualClientId stays null and status NOT_VIABLE here —
    // mirrors SyncService: a household only becomes ACTIVE with a head
    // once a HEAD Membership is applied (see applyHeadOfHouseholdRulesLocally).
    fun buildHousehold(clientId: String, locationId: Long, fields: Map<String, String>): Household = Household(
        clientId = clientId,
        serverId = null,
        householdCode = fields["householdCode"]?.takeIf { it.isNotBlank() },
        locationId = locationId,
        latitude = fields["latitude"]?.toDoubleOrNull(),
        longitude = fields["longitude"]?.toDoubleOrNull(),
        headIndividualClientId = null,
        status = HouseholdStatus.NOT_VIABLE,
        synced = false
    )

    fun buildMembership(
        clientId: String,
        individualClientId: String,
        householdClientId: String,
        fields: Map<String, String>
    ): Membership = Membership(
        clientId = clientId,
        serverId = null,
        individualClientId = individualClientId,
        householdClientId = householdClientId,
        relationshipToHead = RelationshipToHead.valueOf(fields.getValue("relationshipToHead")),
        startDate = LocalDate.parse(fields.getValue("startDate")),
        startType = MembershipStartType.valueOf(fields.getValue("startType")),
        endDate = fields["endDate"]?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) },
        endType = fields["endType"]?.takeIf { it.isNotBlank() }?.let { MembershipEndType.valueOf(it) },
        synced = false
    )

    // Mirrors SyncService.applyHeadOfHouseholdRules exactly, on purpose —
    // same name, same condition, same effect — so the two are easy to
    // compare and keep in sync if the backend rule ever changes.
    // CAUTION: this logic now lives in two places (Java + Kotlin) and
    // can drift; there is no shared source of truth between them.
    suspend fun applyHeadOfHouseholdRulesLocally(
        membership: Membership,
        householdDao: HouseholdDao
    ) {
        if (membership.relationshipToHead != RelationshipToHead.HEAD) return
        val household = householdDao.getByClientId(membership.householdClientId) ?: return

        if (membership.endDate == null) {
            householdDao.update(
                household.copy(
                    headIndividualClientId = membership.individualClientId,
                    status = HouseholdStatus.ACTIVE
                )
            )
        } else if (household.headIndividualClientId == membership.individualClientId) {
            householdDao.update(
                household.copy(
                    headIndividualClientId = null,
                    status = HouseholdStatus.NOT_VIABLE
                )
            )
        }
    }
}