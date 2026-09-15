package com.andrew.hdss.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.ui.screens.HomeScreen
import com.andrew.hdss.ui.screens.LoginScreen
import com.andrew.hdss.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

object Routes {
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
        startDestination = Routes.LOGIN
    ) {
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