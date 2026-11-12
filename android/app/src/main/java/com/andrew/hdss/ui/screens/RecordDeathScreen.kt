package com.andrew.hdss.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.viewmodels.DeathFlowStep
import com.andrew.hdss.ui.viewmodels.RecordDeathViewModel
import kotlinx.coroutines.launch

@Composable
fun RecordDeathScreen(
    householdClientId: String,
    deceasedClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: RecordDeathViewModel = viewModel(
        key = "record-death-$householdClientId-$deceasedClientId",
        factory = RecordDeathViewModel.factory(householdClientId, deceasedClientId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var cleanupFailed by remember { mutableStateOf(false) }

    val cancel: () -> Unit = {
        scope.launch {
            if (viewModel.abandon()) onCancel() else cleanupFailed = true
        }
    }

    // System back abandons the same way the Cancel button does.
    BackHandler(enabled = state.step == DeathFlowStep.FORM, onBack = cancel)

    LaunchedEffect(state.step) {
        if (state.step == DeathFlowStep.DONE) onComplete()
    }

    when (state.step) {
        DeathFlowStep.LOADING, DeathFlowStep.DONE -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        DeathFlowStep.FORM -> state.formId?.let { formId ->
            FormFillScreen(
                formId = formId,
                visitId = viewModel.currentVisitId,
                context = viewModel.formContext,
                title = "Record death",
                onComplete = viewModel::onFormComplete,
                onCancel = cancel
            )
        }

        DeathFlowStep.ERROR -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = state.errorMessage ?: "Couldn't open the death form.",
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onCancel) { Text("Back") }
        }
    }

    if (cleanupFailed) {
        AlertDialog(
            onDismissRequest = { cleanupFailed = false },
            title = { Text("Couldn't close the form") },
            text = { Text("The unfinished visit couldn't be removed. Try again.") },
            confirmButton = {
                TextButton(onClick = { cleanupFailed = false }) { Text("OK") }
            }
        )
    }
}