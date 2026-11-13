package com.andrew.hdss.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.data.models.enums.RelationshipToHead
import com.andrew.hdss.ui.components.FormDatePicker
import com.andrew.hdss.ui.components.SelectableRow
import com.andrew.hdss.ui.labels.displayLabel
import com.andrew.hdss.ui.viewmodels.RecordTransferInViewModel
import com.andrew.hdss.ui.viewmodels.TransferInStep
import com.andrew.hdss.ui.viewmodels.TransferInUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTransferInScreen(
    destinationHouseholdClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: RecordTransferInViewModel = viewModel(
        key = "transfer-in-$destinationHouseholdClientId",
        factory = RecordTransferInViewModel.factory(destinationHouseholdClientId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.step) {
        if (state.step == TransferInStep.DONE) onComplete()
    }

    val back: () -> Unit = {
        if (state.step == TransferInStep.DETAILS) viewModel.backToSearch() else onCancel()
    }

    BackHandler(onBack = back)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer in") },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding)
        when (state.step) {
            TransferInStep.SELECT_PERSON -> PersonSearchStep(state, viewModel, modifier)
            TransferInStep.DETAILS -> TransferDetailsStep(state, viewModel, modifier)
            TransferInStep.DONE -> Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun PersonSearchStep(
    state: TransferInUiState,
    viewModel: RecordTransferInViewModel,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search by name") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
            )
        }

        state.message?.let { message ->
            item {
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }

        if (state.candidates.isEmpty()) {
            item {
                Text(
                    text = if (state.searchQuery.isBlank()) {
                        "No one on this device can transfer in."
                    } else {
                        "No match. They may not be on this device, or not currently in another " +
                                "household. For someone new, use Individual Registration."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(state.candidates, key = { it.clientId }) { individual ->
            Card(
                onClick = { viewModel.selectPerson(individual) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${individual.firstName} ${individual.lastName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = individual.dateOfBirth?.let { "born $it" } ?: "Date of birth unknown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferDetailsStep(
    state: TransferInUiState,
    viewModel: RecordTransferInViewModel,
    modifier: Modifier
) {
    val person = state.selectedPerson ?: return

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${person.firstName} ${person.lastName}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Moving from another household on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        state.message?.let { message ->
            item {
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }

        item {
            Text("Date of transfer", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            FormDatePicker(
                value = state.transferDate,
                onValueChange = viewModel::onTransferDateChange
            )
        }

        item {
            Text("Relationship to head", style = MaterialTheme.typography.labelLarge)
        }

        val relationships = RelationshipToHead.entries.filter {
            it != RelationshipToHead.HEAD || !state.destinationHasHead
        }
        items(relationships, key = { it.name }) { option ->
            SelectableRow(
                title = option.displayLabel(),
                selected = state.relationshipToHead == option,
                onClick = { viewModel.onRelationshipChange(option) }
            )
        }

        if (state.wasHead) {
            item {
                Text(
                    "This person was head of the previous household. " +
                            "Are other members still living there?",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            item {
                SelectableRow(
                    title = "Yes",
                    selected = state.otherMembersRemain == true,
                    onClick = { viewModel.onOtherMembersRemainChange(true) }
                )
            }
            item {
                SelectableRow(
                    title = "No",
                    selected = state.otherMembersRemain == false,
                    onClick = { viewModel.onOtherMembersRemainChange(false) }
                )
            }
        }

        if (state.needsNewHead) {
            item {
                Text(
                    "Who is the new head of the previous household?",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (state.previousHouseholdMembers.isEmpty()) {
                item {
                    Text(
                        "No other members to choose from.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            items(state.previousHouseholdMembers, key = { it.clientId }) { member ->
                SelectableRow(
                    title = "${member.firstName} ${member.lastName}",
                    selected = state.previousNewHeadClientId == member.clientId,
                    onClick = { viewModel.onPreviousNewHeadChange(member.clientId) }
                )
            }
        }

        item {
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Text(if (state.isSaving) "Saving…" else "Record transfer")
            }
        }
    }
}

private fun relationshipLabel(relationship: RelationshipToHead): String = when (relationship) {
    RelationshipToHead.HEAD -> "Head"
    RelationshipToHead.SPOUSE -> "Spouse"
    RelationshipToHead.CHILD -> "Child"
    RelationshipToHead.PARENT -> "Parent"
    RelationshipToHead.SIBLING -> "Sibling"
    RelationshipToHead.OTHER_RELATIVE -> "Other relative"
    RelationshipToHead.NON_RELATIVE -> "Non-relative"
    else -> relationship.name
}