package com.andrew.hdss.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

val Context.serverDataStore: DataStore<Preferences> by preferencesDataStore(name = "server_settings")

class ServerSettings(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope
) {
    private val key = stringPreferencesKey("base_url")
    private val loaded = CompletableDeferred<Unit>()
    private val _address = MutableStateFlow<HttpUrl?>(null)

    val address: StateFlow<HttpUrl?> = _address.asStateFlow()

    val currentBaseUrl: HttpUrl? get() = _address.value

    val hasAddress: Boolean get() = _address.value != null

    init {
        scope.launch {
            dataStore.data.first()[key]?.toHttpUrlOrNull()?.let { _address.value = it }
            loaded.complete(Unit)
        }
    }

    suspend fun awaitLoaded() = loaded.await()

    suspend fun set(url: HttpUrl) {
        loaded.await()
        dataStore.edit { it[key] = url.toString() }
        _address.value = url
    }

    companion object {
        const val PLACEHOLDER_BASE_URL = "http://localhost/"

        // Accepts "example.com", "https://example.com", or "https://example.com/api/".
        // Always returns a base URL ending in /api/.
        fun parse(raw: String): HttpUrl? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null

            val withScheme = if ("://" in trimmed) trimmed else "https://$trimmed"
            val base = withScheme.toHttpUrlOrNull() ?: return null

            val path = base.encodedPath.trimEnd('/')
            val withApi = if (path.endsWith("/api")) {
                base
            } else {
                base.newBuilder().encodedPath("$path/api").build()
            }

            return withApi.newBuilder()
                .encodedPath(withApi.encodedPath.trimEnd('/') + "/")
                .build()
        }
    }
}