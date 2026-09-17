package com.andrew.hdss.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.ui.DownloadStepStatus
import com.andrew.hdss.ui.theme.AndroidTheme
import com.andrew.hdss.ui.theme.DarkSuccess
import com.andrew.hdss.ui.theme.LightSuccess
import com.andrew.hdss.ui.viewmodels.DownloadDatabaseUiState
import com.andrew.hdss.ui.viewmodels.DownloadDatabaseViewModel
import com.andrew.hdss.ui.viewmodels.DownloadStepUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDatabaseScreen(
    onNavigateBack: () -> Unit,
    viewModel: DownloadDatabaseViewModel = viewModel(factory = DownloadDatabaseViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Database") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.isDownloading) {
                                Toast.makeText(
                                    context,
                                    "Download in progress",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = LocalContentColor.current.copy(
                                alpha = if (uiState.isDownloading) 0.38f else 1f
                            )
                        )
                    }
                }
            )        }
    ) { innerPadding ->
        DownloadDatabaseContent(
            uiState = uiState,
            onStartDownload = viewModel::startDownload,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun DownloadDatabaseContent(
    uiState: DownloadDatabaseUiState,
    onStartDownload: () -> Unit,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onStartDownload()
    }

    fun startWithPermissionCheck() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onStartDownload()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(24.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(uiState.steps) { step -> DownloadStepRow(step) }
        }

        Button(
            onClick = ::startWithPermissionCheck,
            enabled = uiState.canStartDownload,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isDownloading) "Downloading…" else "Start Download")
        }
    }
}

@Composable
private fun DownloadStepRow(step: DownloadStepUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = step.label, style = MaterialTheme.typography.bodyLarge)
        StepStatusIndicator(step.status)
    }
}

@Composable
private fun StepStatusIndicator(status: DownloadStepStatus) {
    when (status) {
        is DownloadStepStatus.Pending -> Icon(
            imageVector = Icons.Outlined.HourglassEmpty,
            contentDescription = "Pending",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        is DownloadStepStatus.InProgress -> CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
        )

        is DownloadStepStatus.Success -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${status.count}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(Icons.Outlined.CheckCircle, contentDescription = "Success", tint = successColor())
        }

        is DownloadStepStatus.Failure -> Icon(
            imageVector = Icons.Outlined.Error,
            contentDescription = status.message,
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun successColor() = if (isSystemInDarkTheme()) DarkSuccess else LightSuccess

private val previewUiState = DownloadDatabaseUiState(
    steps = listOf(
        DownloadStepUiState(label = "Locations", status = DownloadStepStatus.Success(count = 1245)),
        DownloadStepUiState(label = "Individuals", status = DownloadStepStatus.InProgress),
        DownloadStepUiState(label = "Households", status = DownloadStepStatus.Pending),
        DownloadStepUiState(
            label = "Memberships",
            status = DownloadStepStatus.Failure(message = "Could not contact the server")
        )
    ),
    isDownloading = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadDatabasePreviewScaffold(uiState: DownloadDatabaseUiState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Database") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = LocalContentColor.current.copy(
                                alpha = if (uiState.isDownloading) 0.38f else 1f
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        DownloadDatabaseContent(
            uiState = uiState,
            onStartDownload = {},
            contentPadding = innerPadding
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun DownloadDatabaseScreenPreviewLight() {
    AndroidTheme(darkTheme = false, dynamicColor = false) {
        DownloadDatabasePreviewScaffold(previewUiState)
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DownloadDatabaseScreenPreviewDark() {
    AndroidTheme(darkTheme = true, dynamicColor = false) {
        DownloadDatabasePreviewScaffold(previewUiState)
    }
}