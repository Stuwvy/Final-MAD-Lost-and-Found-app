// file: AuthData.java
package com.example.back2me;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthData {

    // IMPORTANT: Replace this with the GUID from your API backend
    private static final String DB_GUID = "19fedb0f-2c1a-4420-a662-129091657404";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_USER_ID = "user_id";

    private static SharedPreferences sharedPreferences;

    // Private constructor to prevent instantiation
    private AuthData() {}

    // 1. Database GUID (Used by RetrofitClient Interceptor)
    public static String getDbName() {
        return DB_GUID;
    }

    // 2. User ID (Used when creating a new item)
    public static void initialize(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setUserId(String userId) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
        }
    }

    public static String getCurrentUserId() {
        if (sharedPreferences == null) {
            return "dummy_firebase_user_id";
        }
        String userId = sharedPreferences.getString(KEY_USER_ID, "dummy_firebase_user_id");
        return userId != null ? userId : "dummy_firebase_user_id";
    }
}