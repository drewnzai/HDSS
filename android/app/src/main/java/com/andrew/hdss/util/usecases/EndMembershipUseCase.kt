package com.andrew.hdss.util.usecases

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
import com.andrew.hdss.util.AnswerEntityMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class FormAnswer(val questionId: Long, val value: String)

data class EndMembershipInput(
    val formId: Long,
    val formVersion: Int,
    val visitId: String,
    val startedAt: LocalDateTime,
    val householdClientId: String,
    val leavingClientId: String,
    val endDate: LocalDate,
    val endType: MembershipEndType,
    val wasHead: Boolean,
    val otherMembersRemain: Boolean,
    val newHeadClientId: String?,
    val informantClientId: String?,
    val answers: List<FormAnswer>
)

sealed interface EndMembershipResult {
    data object Saved : EndMembershipResult
    data class Rejected(val message: String) : EndMembershipResult
}

class EndMembershipUseCase(
    private val db: AppDatabase,
    private val today: () -> LocalDate = { LocalDate.now() }
) {
    suspend operator fun invoke(input: EndMembershipInput): EndMembershipResult {
        if (input.endDate.isAfter(today())) {
            return EndMembershipResult.Rejected("The date can't be in the future.")
        }

        if (input.informantClientId != null && input.informantClientId == input.leavingClientId) {
            return EndMembershipResult.Rejected("The person reporting can't be the person who left.")
        }

        if (input.newHeadClientId != null && input.newHeadClientId == input.leavingClientId) {
            return EndMembershipResult.Rejected("The new head can't be the person who left.")
        }

        return try {
            db.withTransaction { write(input) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The transaction has rolled back, so nothing was partially written.
            EndMembershipResult.Rejected("Couldn't save the record. Nothing was changed.")
        }
    }

    private suspend fun write(input: EndMembershipInput): EndMembershipResult {
        val memberships = db.membershipDao()
        val households = db.householdDao()

        // Everything is read inside the transaction, so the checks see what gets written.
        val leaving = openMembership(input.householdClientId, input.leavingClientId)
            ?: return EndMembershipResult.Rejected(
                "This person is no longer an open member of the household."
            )

        if (input.endDate.isBefore(leaving.startDate)) {
            return EndMembershipResult.Rejected(
                "The date can't be before ${leaving.startDate}, when they joined this household."
            )
        }

        input.informantClientId?.let { id ->
            openMembership(input.householdClientId, id)
                ?: return EndMembershipResult.Rejected(
                    "The person reporting must be a current member of this household."
                )
        }

        // Head status comes from the stored membership. If the answer disagrees, the
        // form is out of date and must not guess which record is right.
        val storedWasHead = leaving.relationshipToHead == RelationshipToHead.HEAD
        if (input.wasHead != storedWasHead) {
            return EndMembershipResult.Rejected(
                "The record for this person has changed since the form was opened. Please start again."
            )
        }

        val newHead: Membership? = if (storedWasHead && input.otherMembersRemain) {
            val id = input.newHeadClientId
                ?: return EndMembershipResult.Rejected("Choose the new head of household.")
            openMembership(input.householdClientId, id)
                ?: return EndMembershipResult.Rejected(
                    "The new head is no longer a member of this household."
                )
        } else {
            null
        }

        // End first, so the head rule clears the old head before the new one is set.
        val ended = leaving.copy(
            endDate = input.endDate,
            endType = input.endType,
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

        db.visitDao().update(
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
                formId = input.formId,
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

        return EndMembershipResult.Saved
    }

    private suspend fun openMembership(
        householdClientId: String,
        individualClientId: String
    ): Membership? =
        db.membershipDao()
            .getCurrentMembers(householdClientId)
            .first()
            .firstOrNull { it.individualClientId == individualClientId }
}