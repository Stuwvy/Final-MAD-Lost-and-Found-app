// file: AuthData.kt
package com.example.back2me

import android.content.Context
import android.content.SharedPreferences

object AuthData {

    // IMPORTANT: Replace this with the GUID from your API backend
    private const val DB_GUID = "19fedb0f-2c1a-4420-a662-129091657404"
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_USER_ID = "user_id"

    // 1. Database GUID (Used by RetrofitClient Interceptor)
    fun getDbName(): String {
        return DB_GUID
    }

    // 2. User ID (Used when creating a new item)
    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getCurrentUserId(): String {
        return sharedPreferences.getString(KEY_USER_ID, "dummy_firebase_user_id") ?: "dummy_firebase_user_id"
    }
}