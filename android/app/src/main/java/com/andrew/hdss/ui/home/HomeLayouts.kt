package com.andrew.hdss.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class HomeTab(val label: String, val icon: ImageVector) {
    PLACES("Places", Icons.Outlined.Place),
    HOUSEHOLD("Household", Icons.Outlined.Home),
    FORMS("Forms", Icons.Outlined.Description)
}

@Composable
fun PhoneHomeLayout(
    topBar: @Composable () -> Unit,
    contextBar: @Composable () -> Unit,
    hasLocationLeaf: Boolean,
    places: @Composable () -> Unit,
    household: @Composable () -> Unit,
    forms: @Composable () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(HomeTab.PLACES) }

    LaunchedEffect(hasLocationLeaf) {
        if (!hasLocationLeaf) tab = HomeTab.PLACES
    }

    Scaffold(
        topBar = {
            Column {
                topBar()
                contextBar()
            }
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        enabled = item == HomeTab.PLACES || hasLocationLeaf,
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (tab) {
                HomeTab.PLACES -> places()
                HomeTab.HOUSEHOLD -> household()
                HomeTab.FORMS -> forms()
            }
        }
    }
}

@Composable
fun WideHomeLayout(
    topBar: @Composable () -> Unit,
    contextBar: @Composable () -> Unit,
    places: @Composable () -> Unit,
    households: @Composable () -> Unit,
    forms: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                topBar()
                contextBar()
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Pane(modifier = Modifier.width(260.dp)) { places() }
            Pane(modifier = Modifier.weight(1f)) { households() }
            Pane(modifier = Modifier.weight(1.2f)) { forms() }
        }
    }
}

@Composable
private fun Pane(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun ContextBar(parts: List<String>) {
    Text(
        text = parts.joinToString(" › ").ifEmpty { "No location selected" },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}