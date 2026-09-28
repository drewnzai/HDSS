package com.andrew.hdss.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.viewmodels.BaselineFlowStep
import com.andrew.hdss.ui.viewmodels.BaselineFlowViewModel

@Composable
fun BaselineFlowScreen(
    locationId: Long,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: BaselineFlowViewModel = viewModel(factory = BaselineFlowViewModel.factory(locationId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.step) {
        BaselineFlowStep.LOADING -> LoadingState()

        BaselineFlowStep.ERROR -> ErrorState(
            message = uiState.errorMessage ?: "Something went wrong.",
            onCancel = onCancel
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
                    onCancel = onCancel
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
                    onCancel = viewModel::onHouseholdFormComplete
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