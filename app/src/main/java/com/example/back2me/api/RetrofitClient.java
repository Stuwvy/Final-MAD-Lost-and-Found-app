// file: RetrofitClient.java
package com.example.back2me.api;

import android.util.Log;
import com.example.back2me.AuthData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String BASE_URL = "https://expense-tracker-db-one.vercel.app/";
    private static final String TAG = "RetrofitClient";

    private static Retrofit retrofit;
    private static ItemApiService itemApiService;

    private RetrofitClient() {}

    // Get Retrofit instance
    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // CRITICAL: This interceptor adds the X-DB-NAME header to EVERY request
            Interceptor dbHeaderInterceptor = chain -> {
                String dbName = AuthData.getDbName();
                Log.d(TAG, "Adding X-DB-NAME header: " + dbName);

                Request originalRequest = chain.request();
                Request newRequest = originalRequest.newBuilder()
                        .addHeader("X-DB-NAME", dbName)
                        .build();

                Log.d(TAG, "Request URL: " + newRequest.url());
                Log.d(TAG, "Request Headers: " + newRequest.headers());

                return chain.proceed(newRequest);
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(dbHeaderInterceptor)  // Must be FIRST
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    // Get API service
    public static ItemApiService getItemApiService() {
        if (itemApiService == null) {
            itemApiService = getRetrofitInstance().create(ItemApiService.class);
        }
        return itemApiService;
    }

    // Utility method to get current ISO date
    public static String getCurrentISODate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }
}