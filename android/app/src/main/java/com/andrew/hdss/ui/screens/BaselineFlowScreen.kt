package com.andrew.hdss.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.andrew.hdss.ui.viewmodels.BaselineFlowStep
import com.andrew.hdss.ui.viewmodels.BaselineFlowViewModel
import kotlinx.coroutines.launch

@Composable
fun BaselineFlowScreen(
    locationId: Long,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: BaselineFlowViewModel = viewModel(factory = BaselineFlowViewModel.factory(locationId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCleanupFailedDialog by remember { mutableStateOf(false) }

    fun proceedWithCancel() {
        scope.launch {
            val cleanedUp = viewModel.abandon()
            if (cleanedUp) {
                onCancel()
            } else {
                showCleanupFailedDialog = true
            }
        }
    }

    val requestCancel: () -> Unit = {
        if (uiState.step == BaselineFlowStep.LOADING || uiState.step == BaselineFlowStep.ERROR) {
            proceedWithCancel()
        } else {
            showConfirmDialog = true
        }
    }

    BackHandler(enabled = uiState.step != BaselineFlowStep.DONE) {
        requestCancel()
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Discard this registration?") },
            text = { Text("What's been entered so far will be deleted. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    proceedWithCancel()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Keep going") }
            }
        )
    }

    if (showCleanupFailedDialog) {
        AlertDialog(
            onDismissRequest = { /* must choose an action below */ },
            title = { Text("Couldn't fully clean up") },
            text = {
                Text(
                    "Some data from this registration couldn't be removed from this device. " +
                            "You can leave anyway, or try again."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCleanupFailedDialog = false
                    onCancel()
                }) { Text("Leave anyway") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCleanupFailedDialog = false
                    proceedWithCancel()
                }) { Text("Try again") }
            }
        )
    }

    when (uiState.step) {
        BaselineFlowStep.LOADING -> LoadingState()

        BaselineFlowStep.ERROR -> ErrorState(
            message = uiState.errorMessage ?: "Something went wrong.",
            onCancel = requestCancel
        )

        BaselineFlowStep.HOUSEHOLD_FORM -> {
            val formId = uiState.householdFormId
            if (formId != null) {
                FormFillScreen(
                    formId = formId,
                    visitId = viewModel.currentVisitId,
                    context = viewModel.householdFormContext,
                    title = "Household registration",
                    onComplete = viewModel::onHouseholdFormComplete,
                    onCancel = requestCancel
                )
            }
        }

        BaselineFlowStep.HEAD_FORM -> {
            val formId = uiState.headFormId
            if (formId != null) {
                FormFillScreen(
                    formId = formId,
                    visitId = viewModel.currentVisitId,
                    context = viewModel.headFormContext,
                    title = "Register head of household",
                    onComplete = viewModel::onHeadFormComplete,
                    onCancel = requestCancel
                )
            }
        }

        BaselineFlowStep.DONE -> {
            LaunchedEffect(Unit) { onComplete() }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onCancel: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCancel) { Text("Go back") }
        }
    }
}