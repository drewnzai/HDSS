package com.andrew.hdss.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.andrew.hdss.ui.viewmodels.FormFillContext
import com.andrew.hdss.util.RecordDeathUseCase
import java.util.UUID

@Composable
fun RecordDeathScreen(
    householdClientId: String,
    deceasedClientId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    // Saved across recomposition and rotation, so a recreated screen writes
    // the death against the same visit.
    val visitId = rememberSaveable { UUID.randomUUID().toString() }

    val context = remember(householdClientId, deceasedClientId) {
        FormFillContext(
            foreignKeys = mapOf("householdClientId" to householdClientId),
            presetAnswers = mapOf("deceased_clientId" to deceasedClientId)
        )
    }

    FormFillScreen(
        formId = RecordDeathUseCase.DEATH_FORM_ID,
        visitId = visitId,
        context = context,
        title = "Record death",
        onComplete = onComplete,
        onCancel = onCancel
    )
}