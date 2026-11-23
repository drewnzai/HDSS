package com.andrew.hdss.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.andrew.hdss.data.models.Location

private const val PLACES_SEARCH_THRESHOLD = 8

@Composable
fun PlacesPane(
    firstName: String,
    path: List<Location>,
    level: List<Location>,
    isLeaf: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onRootClick: () -> Unit,
    onCrumbClick: (Int) -> Unit,
    onLocationSelected: (Location) -> Unit
) {
    val visible = if (query.isBlank()) {
        level
    } else {
        level.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.code?.contains(query, ignoreCase = true) == true
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(key = "greeting") {
            Greeting(firstName = firstName, path = path, isLeaf = isLeaf)
        }

        item(key = "breadcrumbs") {
            LocationBreadcrumbs(
                path = path,
                onRootClick = onRootClick,
                onCrumbClick = onCrumbClick
            )
        }

        if (!isLeaf && level.size > PLACES_SEARCH_THRESHOLD) {
            item(key = "search") {
                SearchField(query = query, onQueryChange = onQueryChange)
            }
        }

        when {
            isLeaf -> item(key = "confirmed") {
                ConfirmedCard(path = path)
            }

            visible.isEmpty() -> item(key = "empty") {
                Text(
                    text = if (query.isBlank()) "No locations here." else "No locations match \"$query\".",
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
}

@Composable
private fun Greeting(firstName: String, path: List<Location>, isLeaf: Boolean) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            text = if (firstName.isNotBlank()) "Hello, $firstName" else "Hello",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = when {
                path.isEmpty() -> "Choose the area you're working in today."
                isLeaf -> "Location confirmed."
                else -> "Keep narrowing down to the lowest level."
            },
            style = MaterialTheme.typography.bodyMedium,
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
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                }
            }
        }
    )
}

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
                location.code?.let {
                    Text(
                        text = it,
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
private fun ConfirmedCard(path: List<Location>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ready to begin", style = MaterialTheme.typography.labelLarge)
            }
            Text(
                text = path.last().name,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = path.joinToString(" › ") { it.name },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}