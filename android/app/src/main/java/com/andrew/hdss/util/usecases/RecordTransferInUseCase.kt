package com.andrew.hdss.util.usecases

import androidx.room.withTransaction
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.data.models.Membership
import com.andrew.hdss.data.models.enums.MembershipEndType
import com.andrew.hdss.data.models.enums.MembershipStartType
import com.andrew.hdss.data.models.enums.RelationshipToHead
import com.andrew.hdss.util.AnswerEntityMapper
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

data class TransferInInput(
    val personClientId: String,
    val destinationHouseholdClientId: String,
    val transferDate: LocalDate,
    val relationshipToHead: RelationshipToHead,
    val otherMembersRemain: Boolean,
    val previousNewHeadClientId: String?
)

sealed interface TransferInResult {
    data object Saved : TransferInResult
    data class Rejected(val message: String) : TransferInResult
}

class RecordTransferInUseCase(
    private val db: AppDatabase,
    private val today: () -> LocalDate = { LocalDate.now() }
) {
    suspend operator fun invoke(input: TransferInInput): TransferInResult {
        if (input.transferDate.isAfter(today())) {
            return TransferInResult.Rejected("The transfer date can't be in the future.")
        }

        return try {
            db.withTransaction { writeTransfer(input) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TransferInResult.Rejected("Couldn't save the transfer. Nothing was changed.")
        }
    }

    private suspend fun writeTransfer(input: TransferInInput): TransferInResult {
        val memberships = db.membershipDao()
        val households = db.householdDao()

        val previous = memberships.getCurrentMembership(input.personClientId)
            ?: return TransferInResult.Rejected(
                "This person isn't a current member of any household on this device. " +
                        "Download the correct location, or use Individual Registration for someone new."
            )

        if (previous.householdClientId == input.destinationHouseholdClientId) {
            return TransferInResult.Rejected("This person is already a member of this household.")
        }

        if (input.transferDate.isBefore(previous.startDate)) {
            return TransferInResult.Rejected(
                "The transfer date can't be before ${previous.startDate}, " +
                        "when they joined their current household."
            )
        }

        val destinationMembers = memberships
            .getCurrentMembers(input.destinationHouseholdClientId)
            .first()

        if (input.relationshipToHead == RelationshipToHead.HEAD &&
            destinationMembers.any { it.relationshipToHead == RelationshipToHead.HEAD }
        ) {
            return TransferInResult.Rejected(
                "This household already has a head. Choose a different relationship."
            )
        }

        val wasHead = previous.relationshipToHead == RelationshipToHead.HEAD

        val newPreviousHead: Membership? = if (wasHead && input.otherMembersRemain) {
            val newHeadId = input.previousNewHeadClientId
                ?: return TransferInResult.Rejected("Choose the new head of the previous household.")
            if (newHeadId == input.personClientId) {
                return TransferInResult.Rejected("The new head can't be the person transferring.")
            }
            memberships
                .getCurrentMembers(previous.householdClientId)
                .first()
                .firstOrNull { it.individualClientId == newHeadId }
                ?: return TransferInResult.Rejected(
                    "The new head is no longer a member of the previous household."
                )
        } else {
            null
        }

        val closed = previous.copy(
            endDate = input.transferDate,
            endType = MembershipEndType.TRANSFER_OUT,
            synced = false
        )
        memberships.update(closed)
        AnswerEntityMapper.applyHeadOfHouseholdRulesLocally(closed, households)

        newPreviousHead?.let { member ->
            val promoted = member.copy(
                relationshipToHead = RelationshipToHead.HEAD,
                synced = false
            )
            memberships.update(promoted)
            AnswerEntityMapper.applyHeadOfHouseholdRulesLocally(promoted, households)
        }

        val opened = Membership(
            clientId = UUID.randomUUID().toString(),
            serverId = null,
            individualClientId = input.personClientId,
            householdClientId = input.destinationHouseholdClientId,
            relationshipToHead = input.relationshipToHead,
            startDate = input.transferDate,
            startType = MembershipStartType.TRANSFER_IN,
            endDate = null,
            endType = null,
            synced = false
        )
        memberships.insert(opened)

        if (opened.relationshipToHead == RelationshipToHead.HEAD) {
            AnswerEntityMapper.applyHeadOfHouseholdRulesLocally(opened, households)
        }

        return TransferInResult.Saved
    }
}