package com.andrew.hdss.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal data class FormActions(
    val householdSelected: Boolean,
    val personSelected: Boolean,
    val onRegisterHousehold: () -> Unit,
    val onAddIndividual: () -> Unit,
    val onTransferIn: () -> Unit,
    val onImmigration: () -> Unit,
    val onRecordDeath: () -> Unit
)

@Composable
internal fun FormsSection(
    actions: FormActions,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Forms",
                style = MaterialTheme.typography.titleMedium
            )

            FormGroup(title = "At this location") {
                FormButton(
                    label = "Register new household",
                    onClick = actions.onRegisterHousehold
                )
            }

            FormGroup(
                title = "This household",
                hint = if (actions.householdSelected) null
                else "Select a household to use these forms."
            ) {
                FormButton(
                    label = "Add individual",
                    onClick = actions.onAddIndividual,
                    enabled = actions.householdSelected
                )
                FormButton(
                    label = "Transfer in",
                    onClick = actions.onTransferIn,
                    enabled = actions.householdSelected
                )
                FormButton(
                    label = "Immigration",
                    onClick = actions.onImmigration,
                    enabled = actions.householdSelected
                )
            }

            FormGroup(
                title = "Selected person",
                hint = if (actions.personSelected) null
                else "Select a person in the household to record a death."
            ) {
                FormButton(
                    label = "Record death",
                    onClick = actions.onRecordDeath,
                    enabled = actions.personSelected,
                    destructive = true
                )
            }
        }
    }
}

@Composable
private fun FormGroup(
    title: String,
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )
        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
private fun FormButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    destructive: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        colors = if (destructive) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Text(label)
    }
}