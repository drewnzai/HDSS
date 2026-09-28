package com.andrew.hdss.ui.screens

import android.annotation.SuppressLint
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.data.models.Choice
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.models.Question
import com.andrew.hdss.data.models.enums.QuestionType
import com.andrew.hdss.ui.viewmodels.FormFillContext
import com.andrew.hdss.ui.viewmodels.FormFillViewModel
import com.google.android.gms.location.LocationServices
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormFillScreen(
    formId: Long,
    visitId: String,
    context: FormFillContext,
    title: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: FormFillViewModel = viewModel(
        key = "form-$formId",
        factory = FormFillViewModel.factory(formId, visitId, context)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onComplete()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            val question = uiState.navigableQuestions.getOrNull(uiState.currentIndex)

            if (uiState.form == null || question == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.navigableQuestions.isNotEmpty() && uiState.currentIndex >= uiState.navigableQuestions.size) {
                        CircularProgressIndicator() // between last answer and isComplete flipping
                    } else {
                        CircularProgressIndicator()
                    }
                }
                return@Column
            }

            LinearProgressIndicator(
                progress = { (uiState.currentIndex + 1).toFloat() / uiState.navigableQuestions.size },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            uiState.constraintError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(question.label, style = MaterialTheme.typography.titleLarge)
                question.hint?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))

                QuestionInput(
                    question = question,
                    choices = question.choiceListName?.let { uiState.choicesByListName[it] } ?: emptyList(),
                    currentValue = uiState.answers[question.id],
                    onValueChange = { viewModel.updateAnswer(question, it) },
                    loadHouseholdMembers = { viewModel.loadHouseholdMembers(question) }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { if (uiState.currentIndex == 0) onCancel() else viewModel.goBack() },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving
                ) {
                    Text(if (uiState.currentIndex == 0) "Cancel" else "Back")
                }
                Button(
                    onClick = viewModel::goNext,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving
                ) {
                    val isLast = uiState.currentIndex == uiState.navigableQuestions.size - 1
                    Text(if (uiState.isSaving) "Saving…" else if (isLast) "Finish" else "Next")
                }
            }
        }
    }
}

@Composable
private fun QuestionInput(
    question: Question,
    choices: List<Choice>,
    currentValue: String?,
    onValueChange: (String) -> Unit,
    loadHouseholdMembers: suspend () -> List<Individual>
) {
    when (question.type) {
        QuestionType.TEXT -> OutlinedTextField(
            value = currentValue ?: "",
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )

        QuestionType.INTEGER -> OutlinedTextField(
            value = currentValue ?: "",
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '-' }) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        QuestionType.DECIMAL -> OutlinedTextField(
            value = currentValue ?: "",
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' || c == '-' }) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        QuestionType.DATE -> FormDatePicker(
            value = currentValue?.let { LocalDate.parse(it) },
            onValueChange = { onValueChange(it.toString()) }
        )

        QuestionType.DATETIME -> {
            // GAP: only date is actually captured — no time-of-day
            // picker. No form currently uses DATETIME, so left minimal
            // rather than building a full date+time picker unused.
            FormDatePicker(
                value = currentValue?.let { LocalDate.parse(it.substringBefore("T")) },
                onValueChange = { onValueChange(it.atStartOfDay().toString()) }
            )
        }

        QuestionType.GEOPOINT -> GeopointCapture(
            currentValue = currentValue,
            onValueChange = onValueChange
        )

        QuestionType.SELECT_ONE -> Column {
            choices.forEach { choice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = currentValue == choice.name,
                        onClick = { onValueChange(choice.name) }
                    )
                    Text(choice.label)
                }
            }
        }

        QuestionType.SELECT_MULTIPLE -> {
            // Space-separated selected Choice.name values — see note
            // above on why not comma-separated.
            val selected = currentValue?.split(" ")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            Column {
                choices.forEach { choice ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = choice.name in selected,
                            onCheckedChange = { checked ->
                                val updated = if (checked) selected + choice.name else selected - choice.name
                                onValueChange(updated.joinToString(" "))
                            }
                        )
                        Text(choice.label)
                    }
                }
            }
        }

        QuestionType.SELECT_HOUSEHOLD_MEMBER -> HouseholdMemberPicker(
            currentValue = currentValue,
            loadCandidates = loadHouseholdMembers,
            onValueChange = onValueChange
        )

        QuestionType.NOTE -> {
            // Display-only — label/hint above already shows the note
            // text; no input, no answer produced.
        }

        QuestionType.CALCULATE -> {
            // Never actually reached — navigableQuestions excludes
            // CALCULATE. Present only for exhaustiveness.
        }

        else -> {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDatePicker(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onValueChange(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun GeopointCapture(
    currentValue: String?,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isCapturing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isCapturing = true
            fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    isCapturing = false
                    if (location != null) {
                        // Space-separated, matching ODK's own geopoint
                        // format — the mapper splits on space.
                        onValueChange("${location.latitude} ${location.longitude}")
                    } else {
                        error = "Could not determine location. Try again outdoors."
                    }
                }
                .addOnFailureListener {
                    isCapturing = false
                    error = "Location capture failed: ${it.message}"
                }
        } else {
            error = "Location permission is required."
        }
    }

    Column {
        if (currentValue != null) {
            Text("Captured: $currentValue")
        } else {
            Text("Not captured yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) },
            enabled = !isCapturing
        ) {
            Text(if (isCapturing) "Capturing…" else "Capture location")
        }
    }
}

@Composable
private fun HouseholdMemberPicker(
    currentValue: String?,
    loadCandidates: suspend () -> List<Individual>,
    onValueChange: (String) -> Unit
) {
    var candidates by remember { mutableStateOf<List<Individual>?>(null) }

    LaunchedEffect(Unit) {
        candidates = loadCandidates()
    }

    val list = candidates

    when {
        list == null -> CircularProgressIndicator()

        list.isEmpty() -> Text(
            text = "No matching household members found.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        else -> Column {
            list.forEach { individual ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = currentValue == individual.clientId,
                        onClick = { onValueChange(individual.clientId) }
                    )
                    Text("${individual.firstName} ${individual.lastName}")
                }
            }
        }
    }
}