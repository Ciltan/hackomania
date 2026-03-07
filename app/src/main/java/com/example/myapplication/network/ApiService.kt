package com.example.myapplication.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("api/v1/analyze")
    suspend fun analyze(
        @Part("url") url: RequestBody? = null,
        @Part("text") text: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): ApiAnalysisResponse
}
