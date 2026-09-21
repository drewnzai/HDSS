package com.andrew.hdss.ui.screens

import android.R.attr.maxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    firstName: String,
    onLogout: () -> Unit,
    onNavigateToDownload: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val roots by viewModel.roots.collectAsStateWithLifecycle()
    val selectedPath by viewModel.selectedPath.collectAsStateWithLifecycle()
    val children by viewModel.currentChildren.collectAsStateWithLifecycle()

    val currentLocation = selectedPath.lastOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("HDSS")
                },
                actions = {
                    IconButton(onClick = onNavigateToDownload) {
                        Icon(
                            imageVector = Icons.Outlined.CloudDownload,
                            contentDescription = "Download Database"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 600.dp
            val horizontalPadding = when {
                maxWidth >= 1000.dp -> 48.dp
                maxWidth >= 600.dp -> 32.dp
                else -> 24.dp
            }

            val contentMaxWidth = if (isWideScreen) {
                900.dp
            } else {
                Dp.Infinity
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
                    .then(
                        if (contentMaxWidth != Dp.Infinity) {
                            Modifier.widthIn(max = contentMaxWidth)
                        } else {
                            Modifier
                        }
                    )
                    .align(Alignment.TopCenter)
                    .padding(
                        top = if (isWideScreen) 24.dp else 16.dp,
                        bottom = 16.dp
                    )
            ) {

                if (isWideScreen) {
                    WideHomeHeader(
                        firstName = firstName
                    )
                } else {
                    CompactHomeHeader(
                        firstName = firstName
                    )
                }

                Spacer(
                    modifier = Modifier.height(
                        if (isWideScreen) 28.dp else 20.dp
                    )
                )

                if (roots.isEmpty()) {
                    EmptyLocationState(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LocationSelector(
                        roots = roots,
                        selectedPath = selectedPath,
                        children = children,
                        onLocationSelected = viewModel::selectLocation,
                        onPathLocationSelected = viewModel::selectPathLocation,
                        wide = isWideScreen
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Sign out")
                }
            }
        }
    }
}

@Composable
private fun CompactHomeHeader(
    firstName: String
) {
    Column {
        Text(
            text = "Welcome back${
                if (firstName.isNotBlank()) ", $firstName"
                else ""
            }",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Select a location to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun WideHomeHeader(
    firstName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Welcome back${
                    if (firstName.isNotBlank()) ", $firstName"
                    else ""
                }",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Select a location to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
private fun LocationSelector(
    roots: List<Location>,
    selectedPath: List<Location>,
    children: List<Location>,
    onLocationSelected: (Location) -> Unit,
    onPathLocationSelected: (Int) -> Unit,
    wide: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        LocationBreadcrumbs(
            path = selectedPath,
            onLocationSelected = onPathLocationSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        val locations = if (selectedPath.isEmpty()) {
            roots
        } else {
            children
        }

        if (locations.isEmpty() && selectedPath.isNotEmpty()) {
            SelectedLocationCard(
                path = selectedPath
            )
        } else {
            LocationList(
                locations = locations,
                onLocationSelected = onLocationSelected,
                wide = wide
            )
        }
    }
}


@Composable
private fun LocationList(
    locations: List<Location>,
    onLocationSelected: (Location) -> Unit,
    wide: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = 100.dp,
                max = if (wide) 420.dp else 360.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = locations,
            key = { it.id }
        ) { location ->

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLocationSelected(location)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = if (wide) 20.dp else 16.dp,
                            vertical = if (wide) 18.dp else 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        location.code?.let { code ->
                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}


@Composable
private fun LocationBreadcrumbs(
    path: List<Location>,
    onLocationSelected: (Int) -> Unit
) {
    if (path.isEmpty()) {
        Text(
            text = "Choose a root location",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(path.size) { index ->

            TextButton(
                onClick = {
                    onLocationSelected(index)
                },
                contentPadding = PaddingValues(
                    horizontal = 8.dp
                )
            ) {
                Text(
                    text = path[index].name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (index < path.lastIndex) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
private fun SelectedLocationCard(
    path: List<Location>
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Selected location",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = path.last().name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = path.joinToString(" → ") { it.name },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun EmptyLocationState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No locations available",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Download the database to make locations available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}