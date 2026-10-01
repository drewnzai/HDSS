package com.andrew.hdss.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.viewmodels.AddIndividualStep
import com.andrew.hdss.ui.viewmodels.AddIndividualViewModel
import kotlinx.coroutines.launch

@Composable
fun AddIndividualScreen(
    householdClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddIndividualViewModel = viewModel(
        factory = AddIndividualViewModel.factory(householdClientId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }

    fun proceedWithCancel() {
        scope.launch {
            viewModel.abandon()
            onCancel()
        }
    }

    val requestCancel: () -> Unit = {
        if (uiState.step == AddIndividualStep.LOADING || uiState.step == AddIndividualStep.ERROR) {
            proceedWithCancel()
        } else {
            showConfirmDialog = true
        }
    }

    BackHandler(enabled = uiState.step != AddIndividualStep.DONE) { requestCancel() }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Discard this registration?") },
            text = { Text("What's been entered so far will be lost.") },
            confirmButton = {
                TextButton(onClick = { showConfirmDialog = false; proceedWithCancel() }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Keep going") }
            }
        )
    }

    when (uiState.step) {
        AddIndividualStep.LOADING -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        AddIndividualStep.ERROR -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(uiState.errorMessage ?: "Something went wrong.", color = MaterialTheme.colorScheme.error)
        }

        AddIndividualStep.FORM -> {
            uiState.formId?.let { formId ->
                FormFillScreen(
                    formId = formId,
                    visitId = viewModel.currentVisitId,
                    context = viewModel.formContext,
                    title = "Add individual",
                    onComplete = viewModel::onFormComplete,
                    onCancel = requestCancel
                )
            }
        }

        AddIndividualStep.DONE -> LaunchedEffect(Unit) { onComplete() }
    }
}