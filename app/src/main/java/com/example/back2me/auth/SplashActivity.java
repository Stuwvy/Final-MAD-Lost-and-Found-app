package com.example.back2me.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.back2me.AuthData;
import com.example.back2me.MainActivity;
import com.example.back2me.R;
import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize AuthData so SharedPreferences is ready
        AuthData.initialize(this);

        auth = FirebaseAuth.getInstance();

        // Delay for 2 seconds then check auth status
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthStatus, 2000);
    }

    private void checkAuthStatus() {
        if (auth.getCurrentUser() != null) {
            // User is logged in, go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User not logged in, go to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}