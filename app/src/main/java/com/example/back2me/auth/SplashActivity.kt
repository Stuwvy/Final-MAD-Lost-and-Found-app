package com.example.back2me.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.back2me.MainActivity
import com.example.back2me.R
import com.example.back2me.AuthData // 💡 CRUCIAL: Import AuthData
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 💡 FIX: Initialize AuthData here so SharedPreferences is ready
        // This ensures setUserId/getCurrentUserId works correctly later.
        AuthData.initialize(this)

        auth = FirebaseAuth.getInstance()

        // Delay for 2 seconds then check auth status
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, 2000)
    }

    private fun checkAuthStatus() {
        if (auth.currentUser != null) {
            // User is logged in, go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User not logged in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}