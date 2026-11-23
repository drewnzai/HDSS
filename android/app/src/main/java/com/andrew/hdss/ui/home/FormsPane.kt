package com.andrew.hdss.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun FormsPane(
    items: List<HomeFormItem>,
    selection: HomeFormSelection,
    extraCount: Int,
    onFormClick: (HomeFormItem) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val groups = groupForms(items, query)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Forms",
            style = MaterialTheme.typography.titleLarge
        )

        // Search appears only once there are enough Extra forms to need it.
        if (extraCount > FORM_SEARCH_THRESHOLD) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search forms") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
            )
        }

        if (groups.isEmpty()) {
            Text(
                text = "No forms match \"$query\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FormSection.entries.forEach { section ->
            val sectionGroups = groups.filter { it.section == section }
            if (sectionGroups.isNotEmpty()) {
                Text(
                    text = section.heading(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                sectionGroups.forEach { group ->
                    FormGroup(
                        group = group,
                        selection = selection,
                        onFormClick = onFormClick
                    )
                }
            }
        }
    }
}

@Composable
private fun FormGroup(
    group: FormGroupUi,
    selection: HomeFormSelection,
    onFormClick: (HomeFormItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = group.scope.title(),
            style = MaterialTheme.typography.labelLarge
        )

        // The hint explains why a group is unavailable, shown once per group.
        if (!selection.allows(group.scope)) {
            Text(
                text = group.scope.hint(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        group.items.forEach { item ->
            FormButton(item = item, onClick = onFormClick)
        }
    }
}

@Composable
private fun FormButton(
    item: HomeFormItem,
    onClick: (HomeFormItem) -> Unit
) {
    OutlinedButton(
        onClick = { onClick(item) },
        enabled = item.enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        colors = if (item.destructive) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(item.label)
            item.note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun FormSection.heading(): String = when (this) {
    FormSection.CORE -> "Core forms"
    FormSection.EXTRA -> "Extra forms"
}