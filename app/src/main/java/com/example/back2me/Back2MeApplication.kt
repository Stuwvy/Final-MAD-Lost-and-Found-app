package com.example.back2me

import android.app.Application

class Back2MeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the AuthData shared preference utility
        AuthData.initialize(this)
    }
}