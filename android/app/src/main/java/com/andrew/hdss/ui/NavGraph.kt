package com.andrew.hdss.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.network.AuthResult
import com.andrew.hdss.ui.screens.HomeScreen
import com.andrew.hdss.ui.screens.LoginScreen
import com.andrew.hdss.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant


object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
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
                }
            )
        }
    }
}

@Composable
private fun SplashScreen(
    tokenDataStore: TokenDataStore,
    authViewModel: AuthViewModel,
    onResult: (loggedIn: Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
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
            else -> false // never logged in
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
}