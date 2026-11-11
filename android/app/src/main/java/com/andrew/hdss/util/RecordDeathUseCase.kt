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

private suspend fun openMembership(
    db: AppDatabase,
    householdClientId: String,
    individualClientId: String
): Membership? =
    db.membershipDao()
        .getCurrentMembers(householdClientId)
        .first()
        .firstOrNull { it.individualClientId == individualClientId }

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
        if (input.newHeadClientId == input.deceasedClientId) {
            return DeathResult.Rejected("The new head can't be the person who died.")
        }

        return try {
            db.withTransaction {
                val memberships = db.membershipDao()
                val households = db.householdDao()

                val deceased = openMembership(db, input.householdClientId, input.deceasedClientId)
                    ?: return@withTransaction DeathResult.Rejected(
                        "This person is no longer an open member of the household."
                    )

                val newHead = input.newHeadClientId?.let { id ->
                    openMembership(db, input.householdClientId, id)
                        ?: return@withTransaction DeathResult.Rejected(
                            "The new head is no longer a member of this household."
                        )
                }

                // End the deceased first, so the household rule clears the
                // old head before the new one is set.
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

                val visitId = UUID.randomUUID().toString()
                val formResponseId = UUID.randomUUID().toString()
                val now = LocalDateTime.now()

                db.visitDao().insert(
                    Visit(
                        id = visitId,
                        householdClientId = input.householdClientId,
                        individualClientId = null,
                        visitDate = now,
                        status = VisitStatus.COMPLETED,
                        synced = false
                    )
                )

                db.formResponseDao().insert(
                    FormResponse(
                        id = formResponseId,
                        visitId = visitId,
                        formId = DEATH_FORM_ID,
                        formVersion = 1,
                        status = FormResponseStatus.COMPLETED,
                        startedAt = now,
                        completedAt = now,
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

                DeathResult.Saved

                DeathResult.Saved
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The transaction has rolled back, so nothing was partially written.
            DeathResult.Rejected("Couldn't save the death record. Nothing was changed.")
        }
    }

    companion object {
        // Server form id for death-registration. Confirm this is the local id
        // as well, since the sync DTO refers to forms by server id.
        const val DEATH_FORM_ID = 7L
    }
}