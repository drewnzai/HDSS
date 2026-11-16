package com.andrew.hdss.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
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

    @Volatile
    var currentBaseUrl: HttpUrl? = null
        private set

    val hasAddress: Boolean get() = currentBaseUrl != null

    init {
        scope.launch {
            dataStore.data.first()[key]?.toHttpUrlOrNull()?.let { currentBaseUrl = it }
            loaded.complete(Unit)
        }
    }

    suspend fun awaitLoaded() = loaded.await()

    suspend fun set(url: HttpUrl) {
        loaded.await()
        dataStore.edit { it[key] = url.toString() }
        currentBaseUrl = url
    }

    suspend fun clear() {
        loaded.await()
        dataStore.edit { it.remove(key) }
        currentBaseUrl = null
    }

    companion object {
        const val PLACEHOLDER_BASE_URL = "http://localhost/"

        fun parse(raw: String): HttpUrl? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null
            val withSlash = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
            return withSlash.toHttpUrlOrNull()
        }
    }
}