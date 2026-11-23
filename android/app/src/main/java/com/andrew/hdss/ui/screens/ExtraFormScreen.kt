package com.andrew.hdss.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.components.FormFlowScreen
import com.andrew.hdss.ui.viewmodels.ExtraFormFlowViewModel

@Composable
fun ExtraFormScreen(
    formId: Long,
    householdClientId: String,
    personClientId: String?,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ExtraFormFlowViewModel = viewModel(
        key = "extra-$formId-$householdClientId-${personClientId ?: "none"}",
        factory = ExtraFormFlowViewModel.factory(formId, householdClientId, personClientId)
    )
) {
    FormFlowScreen(
        title = "",
        viewModel = viewModel,
        onComplete = onComplete,
        onCancel = onCancel
    )
}