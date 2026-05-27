package com.example.data.api

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

interface CsbouiraApi {
    @GET("api/drive")
    suspend fun getDriveRoot(): JsonElement

    @GET("api/drive")
    suspend fun getDriveYear(
        @Query("year") year: String
    ): JsonElement
}
