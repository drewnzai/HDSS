package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.network.ServerSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ServerSettingsViewModel(
    private val serverSettings: ServerSettings
) : ViewModel() {

    val savedAddress: StateFlow<String?> = serverSettings.address
        .map { it?.toString() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun useAddress(raw: String): String? {
        val url = ServerSettings.parse(raw)
            ?: return "Enter a full address, such as https://example.com/api/"
        serverSettings.set(url)
        return null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as HdssApplication
                ServerSettingsViewModel(app.container.serverSettings)
            }
        }
    }
}