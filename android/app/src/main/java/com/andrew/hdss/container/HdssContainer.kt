package com.andrew.hdss.container

import android.content.Context
import com.andrew.hdss.BuildConfig
import com.andrew.hdss.data.AppDatabase
import com.andrew.hdss.datastore.TokenDataStore
import com.andrew.hdss.network.AuthApiRepository
import com.andrew.hdss.network.AuthApiService
import com.andrew.hdss.network.AuthInterceptor
import com.andrew.hdss.network.LocationApiRepository
import com.andrew.hdss.network.LocationApiService
import com.andrew.hdss.network.TokenAuthenticator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class HdssContainer(private val context: Context) {
    private val baseUrl: String = BuildConfig.BASE_URL

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val tokenDataStore: TokenDataStore by lazy {
        TokenDataStore(context)
    }

    val database: AppDatabase by lazy{
        AppDatabase.getInstance(context)
    }

    // Auth endpoints only — no authenticator, or a failed refresh would
    // recursively trigger itself trying to refresh its own 401.
    private val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val authApiRepository: AuthApiRepository by lazy {
        authRetrofit.create(AuthApiRepository::class.java)
    }

    val authApiService: AuthApiService by lazy {
        AuthApiService(
            authApiRepository = authApiRepository,
            tokenDataStore = tokenDataStore,
            json = json
        )
    }

    // Everything else — attaches the access token to outgoing requests,
    // and refreshes automatically on a 401.
    private val apiOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenDataStore))
            .authenticator(TokenAuthenticator(tokenDataStore, authApiService))
            .build()
    }

    val apiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(apiOkHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val locationApiRepository: LocationApiRepository by lazy{
        apiRetrofit.create(LocationApiRepository::class.java)
    }

    val locationApiService: LocationApiService by lazy {
        LocationApiService(
            locationApiRepository = locationApiRepository,
            locationDao = database.locationDao(),
            database = database,
            json = json
        )
    }
}