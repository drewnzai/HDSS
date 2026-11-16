package com.andrew.hdss.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class BaseUrlInterceptor(private val serverSettings: ServerSettings) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val base = serverSettings.currentBaseUrl
            ?: throw IOException("No server address is set. Sign in to choose one.")

        val request = chain.request()
        val endpoint = request.url.encodedPath.removePrefix("/")
        val target = base.resolve(endpoint)
            ?: throw IOException("The server address is invalid.")

        val rewritten = target.newBuilder()
            .encodedQuery(request.url.encodedQuery)
            .build()

        return chain.proceed(request.newBuilder().url(rewritten).build())
    }
}