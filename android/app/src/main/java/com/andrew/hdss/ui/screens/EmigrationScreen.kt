package com.andrew.hdss.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.components.FormFlowScreen
import com.andrew.hdss.ui.viewmodels.EmigrationFlowViewModel

@Composable
fun EmigrationScreen(
    householdClientId: String,
    emigrantClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: EmigrationFlowViewModel = viewModel(
        key = "emigration-$householdClientId-$emigrantClientId",
        factory = EmigrationFlowViewModel.factory(householdClientId, emigrantClientId)
    )
) {
    FormFlowScreen(
        title = "Emigration",
        viewModel = viewModel,
        onComplete = onComplete,
        onCancel = onCancel
    )
}