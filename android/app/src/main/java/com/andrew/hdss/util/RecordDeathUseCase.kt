package com.andrew.hdss.util

import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.models.Answer
import com.andrew.hdss.data.models.FormResponse
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.Visit
import com.andrew.hdss.data.models.enums.FormResponseStatus
import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import com.andrew.hdss.data.models.enums.VisitStatus
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

data class DeathAnswer(val questionId: Long, val value: String)

data class DeathInput(
    val visitId: String,
    val startedAt: LocalDateTime,
    val formVersion: Int,
    val householdClientId: String,
    val deceasedClientId: String,
    val dateOfDeath: LocalDate,
    val wasHead: Boolean,
    val otherMembersRemain: Boolean,
    val newHeadClientId: String?,
    val answers: List<DeathAnswer>
)

sealed interface DeathResult {
    data object Saved : DeathResult
    data class Rejected(val message: String) : DeathResult
}

class RecordDeathUseCase(
    private val db: AppDatabase,
    private val today: () -> LocalDate = { LocalDate.now() }
) {
    suspend operator fun invoke(input: DeathInput): DeathResult {
        // Checks that don't need the database run before the transaction.
        if (input.dateOfDeath.isAfter(today())) {
            return DeathResult.Rejected("The date of death can't be in the future.")
        }
        if (input.wasHead && input.otherMembersRemain && input.newHeadClientId == null) {
            return DeathResult.Rejected("Choose the new head of household.")
        }
        if (input.newHeadClientId != null && input.newHeadClientId == input.deceasedClientId) {
            return DeathResult.Rejected("The new head can't be the person who died.")
        }

        return try {
            db.withTransaction { writeDeath(input) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The transaction has rolled back, so nothing was partially written.
            DeathResult.Rejected("Couldn't save the death record. Nothing was changed.")
        }
    }

    private suspend fun writeDeath(input: DeathInput): DeathResult {
        val memberships = db.membershipDao()
        val households = db.householdDao()

        // Re-checked inside the transaction, since the picker may be stale.
        val deceased = openMembership(input.householdClientId, input.deceasedClientId)
            ?: return DeathResult.Rejected(
                "This person is no longer an open member of the household."
            )

        // The new head only applies when the deceased was head and others remain.
        val newHeadId = if (input.wasHead && input.otherMembersRemain) input.newHeadClientId else null
        val newHead = newHeadId?.let { id ->
            openMembership(input.householdClientId, id)
                ?: return DeathResult.Rejected(
                    "The new head is no longer a member of this household."
                )
        }

        // End the deceased first, so the household rule clears the old head
        // before the new one is set.
        val ended = deceased.copy(
            endDate = input.dateOfDeath,
            endType = MembershipEndType.DEATH,
            synced = false
        )
        memberships.update(ended)
        AnswerEntityMapper.applyHeadOfHouseholdRulesLocally(ended, households)

        newHead?.let { member ->
            val promoted = member.copy(
                relationshipToHead = RelationshipToHead.HEAD,
                synced = false
            )
            memberships.update(promoted)
            AnswerEntityMapper.applyHeadOfHouseholdRulesLocally(promoted, households)
        }

        db.visitDao().insert(
            Visit(
                id = input.visitId,
                householdClientId = input.householdClientId,
                individualClientId = null,
                visitDate = input.startedAt,
                status = VisitStatus.COMPLETED,
                synced = false
            )
        )

        val formResponseId = UUID.randomUUID().toString()
        db.formResponseDao().insert(
            FormResponse(
                id = formResponseId,
                visitId = input.visitId,
                formId = DEATH_FORM_ID,
                formVersion = input.formVersion,
                status = FormResponseStatus.COMPLETED,
                startedAt = input.startedAt,
                completedAt = LocalDateTime.now(),
                synced = false
            )
        )

        db.answerDao().insertAll(
            input.answers.map { answer ->
                Answer(
                    id = UUID.randomUUID().toString(),
                    formResponseId = formResponseId,
                    questionId = answer.questionId,
                    value = answer.value,
                    synced = false
                )
            }
        )

        return DeathResult.Saved
    }

    // Reuses getCurrentMembers so the write and the picker share one definition
    // of "open member of this household".
    private suspend fun openMembership(
        householdClientId: String,
        individualClientId: String
    ): Membership? =
        db.membershipDao()
            .getCurrentMembers(householdClientId)
            .first()
            .firstOrNull { it.individualClientId == individualClientId }

    companion object {
        // Server form id for death-registration.
        const val DEATH_FORM_ID = 7L
    }
}