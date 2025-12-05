// file: RetrofitClient.kt (FIXED)
package com.example.back2me.api

import android.util.Log
import com.example.back2me.AuthData
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://expense-tracker-db-one.vercel.app/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // CRITICAL: This interceptor adds the X-DB-NAME header to EVERY request
    private val dbHeaderInterceptor = Interceptor { chain ->
        val dbName = AuthData.getDbName()
        Log.d("RetrofitClient", "Adding X-DB-NAME header: $dbName")

        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .addHeader("X-DB-NAME", dbName)
            .build()

        Log.d("RetrofitClient", "Request URL: ${newRequest.url}")
        Log.d("RetrofitClient", "Request Headers: ${newRequest.headers}")

        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(dbHeaderInterceptor)  // Must be FIRST
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val itemApiService: ItemApiService = retrofit.create(ItemApiService::class.java)
}

fun getCurrentISODate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return dateFormat.format(Date())
}