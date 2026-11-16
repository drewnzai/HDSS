package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.network.ServerSettings

class ServerSettingsViewModel(
    private val serverSettings: ServerSettings
) : ViewModel() {

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