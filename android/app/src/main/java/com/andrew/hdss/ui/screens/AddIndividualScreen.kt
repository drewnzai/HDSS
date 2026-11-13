package com.andrew.hdss.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.components.FormFlowScreen
import com.andrew.hdss.ui.viewmodels.AddIndividualViewModel

@Composable
fun AddIndividualScreen(
    householdClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddIndividualViewModel = viewModel(
        key = "add-individual-$householdClientId",
        factory = AddIndividualViewModel.factory(householdClientId)
    )
) {
    FormFlowScreen(
        title = "Add individual",
        viewModel = viewModel,
        onComplete = onComplete,
        onCancel = onCancel
    )
}