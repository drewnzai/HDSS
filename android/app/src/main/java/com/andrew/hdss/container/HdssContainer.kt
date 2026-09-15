package com.andrew.hdss.container

import android.content.Context
import com.andrew.hdss.BuildConfig
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.network.AuthApiRepository
import com.andrew.hdss.network.AuthApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

class HdssContainer(context: Context) {
    private val baseUrl: String = BuildConfig.BASE_URL

    private val json: Json = Json{
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val authApiRepository: AuthApiRepository by lazy{
        retrofit.create(AuthApiRepository::class.java)
    }

    val tokenDataStore: TokenDataStore by lazy {
        TokenDataStore(context)
    }

    val authApiService: AuthApiService by lazy{
        AuthApiService(
            authApiRepository = authApiRepository,
            tokenDataStore = tokenDataStore,
            json = json
        )
    }

}