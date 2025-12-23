package com.example.back2me;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.back2me.auth.LoginActivity;
import com.example.back2me.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_DARK_MODE = "dark_mode";

    private ActivitySettingsBinding binding;
    private FirebaseAuth auth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupUI();
        loadSettings();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning from Edit Profile
        setupUI();
    }

    private void setupUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            binding.textEmail.setText(user.getEmail());

            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                binding.textUserName.setText(name);
            } else {
                binding.textUserName.setText(user.getEmail());
            }

            // Load profile photo
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile_circle)
                        .error(R.drawable.ic_profile_circle)
                        .circleCrop()
                        .into(binding.imageProfile);
            }
        }
    }

    private void loadSettings() {
        // Load saved preferences
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        boolean darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);

        binding.switchNotifications.setChecked(notificationsEnabled);
        binding.switchDarkMode.setChecked(darkModeEnabled);
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v -> finish());

        // Notifications toggle
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            String message = isChecked ? "Notifications enabled" : "Notifications disabled";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Dark Mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            // Apply theme immediately
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Edit Profile
        binding.layoutEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // Change Password
        binding.layoutChangePassword.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                auth.sendPasswordResetEmail(user.getEmail())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, R.string.password_reset_sent, Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        // Privacy Policy
        binding.layoutPrivacyPolicy.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.privacy_policy)
                    .setMessage("Back2Me respects your privacy.\n\n" +
                            "• We collect only essential data to provide our services\n" +
                            "• Your data is stored securely on Firebase\n" +
                            "• We do not share your information with third parties\n" +
                            "• You can delete your account and data at any time")
                    .setPositiveButton(R.string.ok, null)
                    .show();
        });

        // Terms of Service
        binding.layoutTermsOfService.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.terms_of_service)
                    .setMessage("By using Back2Me, you agree to:\n\n" +
                            "• Use the app for legitimate lost & found purposes\n" +
                            "• Not post inappropriate or false content\n" +
                            "• Respect other users\n" +
                            "• Report any suspicious activity")
                    .setPositiveButton(R.string.ok, null)
                    .show();
        });

        // About
        binding.layoutAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.about_app)
                    .setMessage("Back2Me v1.0\n\n" +
                            "A Lost & Found app to help reunite people with their lost items.\n\n" +
                            "Developed for MAD Final Project 2025")
                    .setPositiveButton(R.string.ok, null)
                    .show();
        });

        // Logout
        binding.buttonLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_confirmation)
                    .setPositiveButton(R.string.logout, (dialog, which) -> {
                        auth.signOut();
                        Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        // Delete Account
        binding.buttonDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_account)
                    .setMessage(R.string.delete_account_confirmation)
                    .setPositiveButton(R.string.delete, (dialog, which) -> deleteAccount())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    private void deleteAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}