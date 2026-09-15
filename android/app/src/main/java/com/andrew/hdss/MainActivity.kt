package com.andrew.hdss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.ui.NavGraph
import com.andrew.hdss.ui.viewmodels.AuthViewModel
import com.andrew.hdss.ui.screens.LoginScreen
import com.andrew.hdss.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as HdssApplication).container

        enableEdgeToEdge()
        setContent {
            AndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        tokenDataStore = container.tokenDataStore,
                        modifier = Modifier
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}