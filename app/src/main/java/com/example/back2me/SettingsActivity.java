package com.example.back2me;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.back2me.auth.LoginActivity;
import com.example.back2me.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseAuth auth;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_DARK_MODE = "dark_mode";

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

    private void setupUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            binding.textEmail.setText(user.getEmail());
            
            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                binding.textName.setText(name);
            } else {
                binding.textName.setText(user.getEmail());
            }
        }
    }

    private void loadSettings() {
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

        // Dark mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Edit profile
        binding.cardEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit profile coming soon", Toast.LENGTH_SHORT).show();
        });

        // Change password
        binding.cardChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Privacy policy
        binding.cardPrivacy.setOnClickListener(v -> {
            Toast.makeText(this, "Privacy policy", Toast.LENGTH_SHORT).show();
        });

        // Terms of service
        binding.cardTerms.setOnClickListener(v -> {
            Toast.makeText(this, "Terms of service", Toast.LENGTH_SHORT).show();
        });

        // About
        binding.cardAbout.setOnClickListener(v -> {
            showAboutDialog();
        });

        // Logout
        binding.cardLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });

        // Delete account
        binding.cardDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
    }

    private void showChangePasswordDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Unable to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Send password reset email to " + user.getEmail() + "?")
                .setPositiveButton("Send", (dialog, which) -> {
                    auth.sendPasswordResetEmail(user.getEmail())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About Back2Me")
                .setMessage("Back2Me v1.0\n\nA Lost & Found app to help reunite people with their lost items.\n\nDeveloped for MAD Final Project 2025")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    auth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        user.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
