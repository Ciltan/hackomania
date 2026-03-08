package com.example.myapplication.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
<<<<<<< HEAD
import retrofit2.http.GET
=======
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
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

<<<<<<< HEAD
    @GET("api/v1/history")
    suspend fun getHistory(): List<ApiAnalysisResponse>

=======
>>>>>>> e35f3cad15a37b11bec279df070c74d92e49c112
    @Multipart
    @POST("api/v1/translate")
    suspend fun translate(
        @Part("text") text: RequestBody? = null,
        @Part("url") url: RequestBody? = null,
        @Part("target_language") targetLanguage: RequestBody
    ): ApiTranslateResponse

    @Multipart
    @POST("api/v1/chat")
    suspend fun chat(
        @Part("claim") claim: RequestBody,
        @Part("analysis_summary") analysisSummary: RequestBody,
        @Part("question") question: RequestBody
    ): ApiChatResponse
}
