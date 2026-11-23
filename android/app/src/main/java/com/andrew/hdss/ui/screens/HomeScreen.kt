package com.andrew.hdss.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.home.ContextBar
import com.andrew.hdss.ui.home.CoreFlow
import com.andrew.hdss.ui.home.FormScope
import com.andrew.hdss.ui.home.FormSection
import com.andrew.hdss.ui.home.FormsPane
import com.andrew.hdss.ui.home.HomeFormItem
import com.andrew.hdss.ui.home.HomeFormSelection
import com.andrew.hdss.ui.home.HouseholdAndMembersPane
import com.andrew.hdss.ui.home.PhoneHomeLayout
import com.andrew.hdss.ui.home.PlacesPane
import com.andrew.hdss.ui.home.WideHomeLayout
import com.andrew.hdss.ui.home.coreFormItems
import com.andrew.hdss.ui.home.extraFormItems
import com.andrew.hdss.ui.viewmodels.HomeViewModel

private val TWO_PANE_MIN_WIDTH = 840.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    firstName: String,
    onLogout: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onNavigateToPush: () -> Unit,
    onStartBaseline: (locationId: Long) -> Unit,
    onAddIndividual: (householdClientId: String) -> Unit,
    onTransferIn: (householdClientId: String) -> Unit,
    onImmigration: (householdClientId: String) -> Unit,
    onRecordDeath: (householdClientId: String, deceasedClientId: String) -> Unit,
    onRecordEmigration: (householdClientId: String, emigrantClientId: String) -> Unit,
    onExtraForm: (formId: Long, householdClientId: String, personClientId: String?) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val roots by viewModel.roots.collectAsStateWithLifecycle()
    val selectedPath by viewModel.selectedPath.collectAsStateWithLifecycle()
    val children by viewModel.currentChildren.collectAsStateWithLifecycle()
    val households by viewModel.householdsAtSelection.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val forms by viewModel.forms.collectAsStateWithLifecycle()

    var query by rememberSaveable(selectedPath.lastOrNull()?.id) { mutableStateOf("") }

    val isLeaf = selectedPath.isNotEmpty() && children.isEmpty()
    val level = if (selectedPath.isEmpty()) roots else children

    val householdCode = households
        .firstOrNull { it.clientId == selection.householdClientId }
        ?.householdCode
    val personName = members
        .firstOrNull { it.clientId == selection.personClientId }
        ?.let { "${it.firstName} ${it.lastName}" }

    val contextParts = buildList {
        addAll(selectedPath.map { it.name })
        householdCode?.let { add(it) }
        personName?.let { add(it) }
    }

    val formSelection = HomeFormSelection(
        hasLeaf = isLeaf,
        householdSelected = selection.householdClientId != null,
        personSelected = selection.personClientId != null
    )
    val formItems = coreFormItems(forms, formSelection) + extraFormItems(forms, formSelection)
    val extraCount = formItems.count { it.section == FormSection.EXTRA }

    val onFormClick: (HomeFormItem) -> Unit = { item ->
        val household = selection.householdClientId
        val person = selection.personClientId

        if (item.section == FormSection.EXTRA) {
            val formId = item.formId
            if (formId != null && household != null) {
                val personForForm = if (item.scope == FormScope.PERSON) person else null
                onExtraForm(formId, household, personForForm)
            }
        } else {
            when (CoreFlow.valueOf(item.key)) {
                CoreFlow.REGISTER_HOUSEHOLD -> selectedPath.lastOrNull()?.let { onStartBaseline(it.id) }
                CoreFlow.ADD_INDIVIDUAL -> household?.let(onAddIndividual)
                CoreFlow.TRANSFER_IN -> household?.let(onTransferIn)
                CoreFlow.IMMIGRATION -> household?.let(onImmigration)
                CoreFlow.DEATH -> if (household != null && person != null) {
                    onRecordDeath(household, person)
                }
                CoreFlow.EMIGRATION -> if (household != null && person != null) {
                    onRecordEmigration(household, person)
                }
            }
        }
    }

    val topBar: @Composable () -> Unit = {
        HomeTopBar(
            onNavigateToDownload = onNavigateToDownload,
            onNavigateToPush = onNavigateToPush,
            onLogout = onLogout
        )
    }

    val places: @Composable () -> Unit = {
        PlacesPane(
            firstName = firstName,
            path = selectedPath,
            level = level,
            isLeaf = isLeaf,
            query = query,
            onQueryChange = { query = it },
            onRootClick = viewModel::clearSelection,
            onCrumbClick = viewModel::selectPathLocation,
            onLocationSelected = viewModel::selectLocation
        )
    }

    val householdsPane: @Composable () -> Unit = {
        HouseholdAndMembersPane(
            isLeaf = isLeaf,
            households = households,
            selectedHouseholdId = selection.householdClientId,
            onSelectHousehold = viewModel::selectHousehold,
            members = members,
            selectedPersonId = selection.personClientId,
            onSelectPerson = viewModel::selectPerson
        )
    }

    val formsPane: @Composable () -> Unit = {
        FormsPane(
            items = formItems,
            selection = formSelection,
            extraCount = extraCount,
            onFormClick = onFormClick
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        when {
            roots.isEmpty() -> Scaffold(topBar = topBar) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyLocationState(onDownload = onNavigateToDownload)
                }
            }

            maxWidth >= TWO_PANE_MIN_WIDTH -> WideHomeLayout(
                topBar = topBar,
                contextBar = { ContextBar(contextParts) },
                places = places,
                households = householdsPane,
                forms = formsPane
            )

            else -> PhoneHomeLayout(
                topBar = topBar,
                contextBar = { ContextBar(contextParts) },
                hasLocationLeaf = isLeaf,
                places = places,
                household = householdsPane,
                forms = formsPane
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onNavigateToDownload: () -> Unit,
    onNavigateToPush: () -> Unit,
    onLogout: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "HDSS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = {
            IconButton(onClick = onNavigateToDownload) {
                Icon(Icons.Outlined.CloudDownload, contentDescription = "Download database")
            }

            IconButton(onClick = onNavigateToPush) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = "Push data")
            }

            // Sign out sits in the overflow menu so it can't be tapped by accident.
            Column {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sign out") },
                        onClick = {
                            menuOpen = false
                            onLogout()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun EmptyLocationState(
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "No locations yet",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Download the database to load the areas you'll be working in.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        FilledTonalButton(
            onClick = onDownload,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download database")
        }
    }
}