package com.andrew.hdss.network

import com.andrew.hdss.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenDataStore: TokenDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { tokenDataStore.accessToken.first() }
        val request = chain.request().newBuilder()
            .apply { accessToken?.let { header("Authorization", "Bearer $it") } }
            .build()
        return chain.proceed(request)
    }
}