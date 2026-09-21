package com.andrew.hdss.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.TextButton
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
                            Icons.Outlined.CloudDownload,
                            contentDescription = "Download Database"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {

            Text(
                text = "Welcome back${
                    if (firstName.isNotBlank()) ", $firstName"
                    else ""
                }",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (roots.isEmpty()) {
                EmptyLocationState()
            } else {

                Text(
                    text = "Select location",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocationBreadcrumbs(
                    path = selectedPath,
                    onLocationSelected = viewModel::selectPathLocation
                )

                Spacer(modifier = Modifier.height(16.dp))

                LocationList(
                    locations = if (selectedPath.isEmpty()) {
                        roots
                    } else {
                        children
                    },
                    onLocationSelected = viewModel::selectLocation
                )

                if (currentLocation != null && children.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Selected location",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedPath.joinToString(" → ") { it.name },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign out")
            }
        }
    }
}

@Composable
private fun LocationList(
    locations: List<Location>,
    onLocationSelected: (Location) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
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
                        .padding(16.dp),
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
                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
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
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(path.size) { index ->

            TextButton(
                onClick = {
                    onLocationSelected(index)
                }
            ) {
                Text(
                    text = path[index].name
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
private fun EmptyLocationState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No locations available",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Download the database to make locations available.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}