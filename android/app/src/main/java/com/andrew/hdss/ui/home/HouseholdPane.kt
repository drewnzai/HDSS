package com.andrew.hdss.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andrew.hdss.data.daos.MemberRow
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.ui.components.SelectableRow
import com.andrew.hdss.ui.labels.displayLabel
import com.andrew.hdss.ui.labels.displayLabel as relationshipLabel

@Composable
fun HouseholdAndMembersPane(
    isLeaf: Boolean,
    households: List<Household>,
    selectedHouseholdId: String?,
    onSelectHousehold: (String) -> Unit,
    members: List<MemberRow>,
    selectedPersonId: String?,
    onSelectPerson: (String) -> Unit
) {
    if (!isLeaf) {
        PaneMessage("Choose a location to see its households.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Households",
            style = MaterialTheme.typography.titleMedium
        )

        if (households.isEmpty()) {
            PaneMessage("No households registered here yet.")
        } else {
            households.forEach { household ->
                SelectableRow(
                    title = household.householdCode ?: "No code",
                    subtitle = household.status.displayLabel(),
                    selected = household.clientId == selectedHouseholdId,
                    onClick = { onSelectHousehold(household.clientId) }
                )
            }
        }

        Text(
            text = "Members",
            style = MaterialTheme.typography.titleMedium
        )

        when {
            selectedHouseholdId == null ->
                PaneMessage("Select a household to see its members.")

            members.isEmpty() ->
                PaneMessage("No current members in this household.")

            else -> members.forEach { member ->
                SelectableRow(
                    title = "${member.firstName} ${member.lastName}",
                    subtitle = listOfNotNull(
                        member.relationshipToHead.relationshipLabel(),
                        member.dateOfBirth?.let { "born $it" }
                    ).joinToString(" · "),
                    selected = member.clientId == selectedPersonId,
                    onClick = { onSelectPerson(member.clientId) }
                )
            }
        }
    }
}

@Composable
private fun PaneMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}