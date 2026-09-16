package com.andrew.hdss.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andrew.hdss.ui.theme.AndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    firstName: String,
    onLogout: () -> Unit,
    onNavigateToDownload: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HDSS") },
                actions = {
                    IconButton(onClick = onNavigateToDownload) {
                        Icon(Icons.Outlined.CloudDownload, contentDescription = "Download Database")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome back${if (firstName.isNotBlank()) ", $firstName" else ""}",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Household records and sync tools will live here.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.weight(0.5f))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign out")
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview(){
    AndroidTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        HomeScreen(
            firstName = "Test",
            onLogout = {},
            onNavigateToDownload = {}
        )
    }

}