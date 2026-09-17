package com.andrew.hdss.network.services

import com.andrew.hdss.dtos.BatchResponse
import com.andrew.hdss.dtos.HouseholdDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface HouseholdApiRepository {

    @GET("api/households")
    suspend fun getHouseholds(
        @QueryMap request: Map<String, String>
    ): Response<BatchResponse<HouseholdDto>>
}

class HouseholdApiService {
}