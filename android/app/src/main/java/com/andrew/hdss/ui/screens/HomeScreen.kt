package com.andrew.hdss.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andrew.hdss.ui.theme.AndroidTheme
import com.andrew.hdss.ui.viewmodels.HomeViewModel
import com.andrew.hdss.data.models.Location
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel


private const val SEARCH_THRESHOLD = 8
private val TWO_PANE_MIN_WIDTH = 840.dp
private val SINGLE_COLUMN_MAX_WIDTH = 640.dp
private val SIDE_PANEL_WIDTH = 360.dp

private data class BrowserState(
    val roots: List<Location>,
    val path: List<Location>,
    val children: List<Location>
) {
    val current: Location? get() = path.lastOrNull()
    val level: List<Location> get() = if (path.isEmpty()) roots else children
    val isLeaf: Boolean get() = path.isNotEmpty() && children.isEmpty()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    firstName: String,
    onLogout: () -> Unit,
    onNavigateToDownload: () -> Unit,
    onStartBaseline: (locationId: Long) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val roots by viewModel.roots.collectAsStateWithLifecycle()
    val selectedPath by viewModel.selectedPath.collectAsStateWithLifecycle()
    val children by viewModel.currentChildren.collectAsStateWithLifecycle()

    val state = BrowserState(roots, selectedPath, children)

    // Reset the search text whenever the user moves to a different level.
    var query by rememberSaveable(state.current?.id) { mutableStateOf("") }

    val startBaseline: () -> Unit = { state.current?.let { onStartBaseline(it.id) } }

    Scaffold(
        topBar = {
            HomeTopBar(
                onNavigateToDownload = onNavigateToDownload,
                onLogout = onLogout
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (roots.isEmpty()) {
                EmptyLocationState(
                    onDownload = onNavigateToDownload,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
            } else if (maxWidth >= TWO_PANE_MIN_WIDTH) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        browserItems(
                            state = state,
                            firstName = firstName,
                            query = query,
                            onQueryChange = { query = it },
                            showInlineReadyPanel = false,
                            onRootClick = viewModel::clearSelection,
                            onCrumbClick = viewModel::selectPathLocation,
                            onLocationSelected = viewModel::selectLocation,
                            onStartBaseline = startBaseline
                        )
                    }

                    SelectionSidePanel(
                        state = state,
                        onStartBaseline = startBaseline,
                        modifier = Modifier
                            .width(SIDE_PANEL_WIDTH)
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = SINGLE_COLUMN_MAX_WIDTH)
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    browserItems(
                        state = state,
                        firstName = firstName,
                        query = query,
                        onQueryChange = { query = it },
                        showInlineReadyPanel = true,
                        onRootClick = viewModel::clearSelection,
                        onCrumbClick = viewModel::selectPathLocation,
                        onLocationSelected = viewModel::selectLocation,
                        onStartBaseline = startBaseline
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onNavigateToDownload: () -> Unit,
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
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = "Download database"
                )
            }

            // Sign out lives in an overflow menu so it can't be hit by
            // accident while someone is tapping through locations.
            Column {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More options"
                    )
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

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.browserItems(
    state: BrowserState,
    firstName: String,
    query: String,
    onQueryChange: (String) -> Unit,
    showInlineReadyPanel: Boolean,
    onRootClick: () -> Unit,
    onCrumbClick: (Int) -> Unit,
    onLocationSelected: (Location) -> Unit,
    onStartBaseline: () -> Unit
) {
    item(key = "greeting") {
        Greeting(firstName = firstName, state = state)
    }

    stickyHeader(key = "breadcrumbs") {
        Surface(color = MaterialTheme.colorScheme.background) {
            LocationBreadcrumbs(
                path = state.path,
                onRootClick = onRootClick,
                onCrumbClick = onCrumbClick
            )
        }
    }

    val level = state.level
    val visible = if (query.isBlank()) {
        level
    } else {
        level.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.code?.contains(query, ignoreCase = true) == true
        }
    }

    if (!state.isLeaf && level.size > SEARCH_THRESHOLD) {
        item(key = "search") {
            SearchField(query = query, onQueryChange = onQueryChange)
        }
    }

    when {
        state.isLeaf -> {
            if (showInlineReadyPanel) {
                item(key = "ready") {
                    ReadyPanel(path = state.path, onStartBaseline = onStartBaseline)
                }
            }
        }

        visible.isEmpty() -> item(key = "no-matches") {
            Text(
                text = "No locations match \"$query\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
        }

        else -> items(items = visible, key = { it.id }) { location ->
            LocationRow(
                location = location,
                onClick = { onLocationSelected(location) }
            )
        }
    }
}

@Composable
private fun Greeting(firstName: String, state: BrowserState) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            text = if (firstName.isNotBlank()) "Hello, $firstName" else "Hello",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when {
                state.path.isEmpty() -> "Choose the area you're working in today."
                state.isLeaf -> "Location confirmed."
                else -> "Keep narrowing down to the lowest level."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LocationBreadcrumbs(
    path: List<Location>,
    onRootClick: () -> Unit,
    onCrumbClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChip(
                selected = path.isEmpty(),
                onClick = onRootClick,
                label = { Text("All areas") }
            )
        }

        itemsIndexed(path) { index, location ->
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterChip(
                selected = index == path.lastIndex,
                onClick = { onCrumbClick(index) },
                label = {
                    Text(
                        text = location.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        placeholder = { Text("Search this level") },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear search"
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRow(
    location: Location,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium
                )

                location.code?.let { code ->
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReadyPanel(
    path: List<Location>,
    onStartBaseline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null
                )
                Text(
                    text = "Ready to begin",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = path.last().name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = path.joinToString(" › ") { it.name },
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Register the household and its head of household at this location.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onStartBaseline,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text("Start baseline")
            }
        }
    }
}

@Composable
private fun SelectionSidePanel(
    state: BrowserState,
    onStartBaseline: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLeaf) {
        ReadyPanel(
            path = state.path,
            onStartBaseline = onStartBaseline,
            modifier = modifier
        )
    } else {
        OutlinedCard(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "No location confirmed yet",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Drill down to the lowest level to start a baseline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
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