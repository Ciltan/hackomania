package com.example.myapplication.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /**
     * For Android Emulator: use http://10.0.2.2:8000/
     * For Physical Device:  use your PC's LAN IP (e.g. http://192.168.x.x:8000/)
     *
     * Current setting: Physical device (LAN IP)
     */
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
