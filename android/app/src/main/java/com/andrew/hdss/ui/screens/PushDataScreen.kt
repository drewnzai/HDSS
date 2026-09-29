package com.andrew.hdss.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.viewmodels.PushDataViewModel
import com.andrew.hdss.ui.viewmodels.PushReport
import com.andrew.hdss.ui.viewmodels.VisitBundle
import java.time.format.DateTimeFormatter

private val VISIT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: PushDataViewModel = viewModel(factory = PushDataViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Push data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = viewModel::push,
                    enabled = state.selectedVisitId != null && !state.isPushing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(min = 56.dp)
                ) {
                    if (state.isPushing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(if (state.isPushing) "Pushing…" else "Push data")
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.error?.let { message ->
                    item(key = "error") {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(message, modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                state.report?.let { report ->
                    item(key = "report") {
                        ReportCard(report = report, onDismiss = viewModel::dismissReport)
                    }
                }

                if (state.bundles.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = "Nothing to push. Completed visits appear here until their data has been uploaded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    item(key = "hint") {
                        Text(
                            text = "Choose a visit. Only the data related to it is sent.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(items = state.bundles, key = { it.visit.id }) { bundle ->
                        VisitCard(
                            bundle = bundle,
                            selected = bundle.visit.id == state.selectedVisitId,
                            enabled = !state.isPushing,
                            onSelect = { viewModel.select(bundle.visit.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitCard(
    bundle: VisitBundle,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        enabled = enabled,
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Visit ${bundle.visit.id}",
                    style = MaterialTheme.typography.titleMedium
                )

                bundle.visit.visitDate?.let {
                    Text(
                        text = it.format(VISIT_DATE_FORMAT),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                bundle.household?.let {
                    DetailRow("Household", it.householdCode ?: "No code")
                }
                if (bundle.individuals.isNotEmpty()) {
                    DetailRow(
                        "Individuals",
                        bundle.individuals.joinToString { "${it.firstName} ${it.lastName}" }
                    )
                }
                if (bundle.memberships.isNotEmpty()) {
                    DetailRow("Memberships", bundle.memberships.size.toString())
                }
                if (bundle.formResponses.isNotEmpty()) {
                    DetailRow("Form responses", bundle.formResponses.size.toString())
                }
                if (bundle.answers.isNotEmpty()) {
                    DetailRow("Answers", bundle.answers.size.toString())
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(112.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReportCard(report: PushReport, onDismiss: () -> Unit) {
    val hasFailures = report.failures.isNotEmpty()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasFailures) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (hasFailures) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (hasFailures) "Pushed with problems" else "Push complete",
                style = MaterialTheme.typography.titleMedium
            )

            report.pushed.forEach { (label, count) ->
                Text("$label: $count sent")
            }

            report.failures.forEach { failure ->
                Text("${failure.what}: ${failure.message}")
            }

            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}