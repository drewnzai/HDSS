package com.andrew.hdss.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenDataStore(private val context: Context) {

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        val USERNAME = stringPreferencesKey("username")
        val FIRST_NAME = stringPreferencesKey("firstName")
        val ROLE = stringPreferencesKey("role")
        val EXPIRES_AT = stringPreferencesKey("expiresAt")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN] }
    val firstName: Flow<String?> = context.dataStore.data.map { it[FIRST_NAME] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME] }
    val expiresAt: Flow<String?> = context.dataStore.data.map { it[EXPIRES_AT] }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        username: String,
        firstName: String,
        role: String,
        expiresAt: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
            prefs[USERNAME] = username
            prefs[FIRST_NAME] = firstName
            prefs[ROLE] = role
            prefs[EXPIRES_AT] = expiresAt
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}