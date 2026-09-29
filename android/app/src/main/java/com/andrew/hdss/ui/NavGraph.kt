package com.andrew.hdss.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.network.AuthResult
import com.andrew.hdss.ui.screens.BaselineFlowScreen
import com.andrew.hdss.ui.screens.DownloadDatabaseScreen
import com.andrew.hdss.ui.screens.HomeScreen
import com.andrew.hdss.ui.screens.LoginScreen
import com.andrew.hdss.ui.screens.PushDataScreen
import com.andrew.hdss.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant


object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val DOWNLOAD_DATABASE = "download_database"
    const val PUSH_DATA = "push_data"
    const val BASELINE_LOCATION_ARG = "locationId"
    const val BASELINE = "baseline/{$BASELINE_LOCATION_ARG}"

    fun baseline(locationId: Long) = "baseline/$locationId"
}

@Composable
fun NavGraph(
    tokenDataStore: TokenDataStore,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val firstName by tokenDataStore.firstName.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                authViewModel = authViewModel,
                tokenDataStore = tokenDataStore,
                onResult = { loggedIn ->
                    navController.navigate(if (loggedIn) Routes.HOME else Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                firstName = firstName ?: "",
                onLogout = {
                    scope.launch {
                        authViewModel.reset()
                        tokenDataStore.clear()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                },
                onNavigateToDownload = { navController.navigate(Routes.DOWNLOAD_DATABASE) },
                onNavigateToPush = { navController.navigate(Routes.PUSH_DATA) },
                onStartBaseline = { locationId ->
                    navController.navigate(Routes.baseline(locationId))
                }
            )
        }

        composable(Routes.DOWNLOAD_DATABASE) {
            DownloadDatabaseScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.PUSH_DATA) {
            PushDataScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.BASELINE,
            arguments = listOf(
                navArgument(Routes.BASELINE_LOCATION_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments
                ?.getLong(Routes.BASELINE_LOCATION_ARG)
                ?: return@composable

            BaselineFlowScreen(
                locationId = locationId,
                onComplete = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
private enum class PermissionPhase {
    CHECKING, RATIONALE, COARSE_ONLY, PERMANENTLY_DENIED, RESOLVED
}

@Composable
private fun SplashScreen(
    tokenDataStore: TokenDataStore,
    authViewModel: AuthViewModel,
    onResult: (loggedIn: Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var phase by remember { mutableStateOf(PermissionPhase.CHECKING) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        phase = when {
            results[Manifest.permission.ACCESS_FINE_LOCATION] == true ->
                PermissionPhase.RESOLVED

            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true ->
                PermissionPhase.COARSE_ONLY

            activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.ACCESS_FINE_LOCATION
                    ) -> PermissionPhase.RATIONALE

            else -> PermissionPhase.PERMANENTLY_DENIED
        }
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            phase = PermissionPhase.RESOLVED
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(phase) {
        if (phase != PermissionPhase.RESOLVED) return@LaunchedEffect

        val accessToken = tokenDataStore.accessToken.first()
        val expiresAt = tokenDataStore.expiresAt.first()

        val stillValid = accessToken != null &&
                expiresAt?.let { Instant.parse(it).isAfter(Instant.now()) } ?: false

        val loggedIn = when {
            stillValid -> true
            accessToken != null -> {
                when (authViewModel.refreshToken()) {
                    is AuthResult.Success -> true
                    else -> {
                        tokenDataStore.clear()
                        false
                    }
                }
            }
            else -> false
        }

        onResult(loggedIn)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    when (phase) {
        PermissionPhase.RATIONALE -> AlertDialog(
            onDismissRequest = { phase = PermissionPhase.RESOLVED },
            title = { Text("Location access") },
            text = {
                Text(
                    "HDSS uses precise location once, to record a household's GPS point during " +
                            "registration. Granting it now means you won't be asked again during a visit."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { phase = PermissionPhase.RESOLVED }) {
                    Text("Not now")
                }
            }
        )

        PermissionPhase.COARSE_ONLY -> AlertDialog(
            onDismissRequest = { phase = PermissionPhase.RESOLVED },
            title = { Text("Precise location needed") },
            text = {
                Text(
                    "Only approximate location was granted. Household GPS points need precise " +
                            "location for accuracy — you can turn this on now, or later when capturing a point."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }) { Text("Try again") }
            },
            dismissButton = {
                TextButton(onClick = { phase = PermissionPhase.RESOLVED }) {
                    Text("Continue anyway")
                }
            }
        )

        PermissionPhase.PERMANENTLY_DENIED -> AlertDialog(
            onDismissRequest = { phase = PermissionPhase.RESOLVED },
            title = { Text("Location access") },
            text = {
                Text(
                    "Location permission was denied. You can enable it later from the app's " +
                            "settings when you need to capture a household's GPS point."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                    phase = PermissionPhase.RESOLVED
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { phase = PermissionPhase.RESOLVED }) {
                    Text("Continue anyway")
                }
            }
        )

        PermissionPhase.CHECKING, PermissionPhase.RESOLVED -> Unit
    }
}