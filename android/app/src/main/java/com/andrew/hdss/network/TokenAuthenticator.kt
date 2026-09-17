package com.andrew.hdss.network

import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.network.services.AuthApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenDataStore: TokenDataStore,
    private val authApiService: AuthApiService,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Already retried once for this request chain — don't loop forever.
        if (responseCount(response) >= 2) return null

        return runBlocking {
            mutex.withLock {
                val requestToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")
                val currentAccessToken = tokenDataStore.accessToken.first()

                // Another request already refreshed while we were waiting on the lock —
                // just retry with the token that's now stored, no need to refresh again.
                if (currentAccessToken != null && currentAccessToken != requestToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccessToken")
                        .build()
                }

                when (val result = authApiService.refreshToken()) {
                    is AuthResult.Success -> {
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${result.response.authenticationToken}")
                            .build()
                    }
                    else -> {
                        tokenDataStore.clear()
                        null // null tells OkHttp to give up — the original 401 surfaces to the caller
                    }
                }
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
