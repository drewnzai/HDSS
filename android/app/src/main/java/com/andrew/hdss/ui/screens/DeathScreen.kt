package com.andrew.hdss.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.components.FormFlowScreen
import com.andrew.hdss.ui.viewmodels.DeathFlowViewModel

@Composable
fun DeathScreen(
    householdClientId: String,
    deceasedClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: DeathFlowViewModel = viewModel(
        key = "death-$householdClientId-$deceasedClientId",
        factory = DeathFlowViewModel.factory(householdClientId, deceasedClientId)
    )
) {
    FormFlowScreen(
        title = "Record death",
        viewModel = viewModel,
        onComplete = onComplete,
        onCancel = onCancel
    )
}