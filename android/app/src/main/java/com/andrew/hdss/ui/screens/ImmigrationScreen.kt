package com.andrew.hdss.ui.screens

import com.andrew.hdss.ui.components.FormFlowScreen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.viewmodels.ImmigrationViewModel

@Composable
fun ImmigrationScreen(
    householdClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ImmigrationViewModel = viewModel(
        key = "immigration-$householdClientId",
        factory = ImmigrationViewModel.factory(householdClientId)
    )
) {
    FormFlowScreen(
        title = "Immigration",
        viewModel = viewModel,
        onComplete = onComplete,
        onCancel = onCancel
    )
}