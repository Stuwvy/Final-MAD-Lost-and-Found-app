package com.example.back2me.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.back2me.MainActivity;
import com.example.back2me.R;
import com.example.back2me.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v -> finish());

        // Sign up button
        binding.buttonSignup.setOnClickListener(v -> attemptSignup());

        // Login link
        binding.textLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void attemptSignup() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();
        String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(email)) {
            binding.layoutEmail.setError("Email is required");
            return;
        }
        binding.layoutEmail.setError(null);

        if (TextUtils.isEmpty(password)) {
            binding.layoutPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            binding.layoutPassword.setError("Password must be at least 6 characters");
            return;
        }
        binding.layoutPassword.setError(null);

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.layoutConfirmPassword.setError("Please confirm your password");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.layoutConfirmPassword.setError("Passwords don't match");
            return;
        }
        binding.layoutConfirmPassword.setError(null);

        // Show loading
        setLoading(true);

        // Create account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Set display name from email
                    String displayName = email.split("@")[0];
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build();

                    if (authResult.getUser() != null) {
                        authResult.getUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(task -> {
                                    setLoading(false);
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSignup.setEnabled(!loading);
        binding.buttonSignup.setText(loading ? "" : getString(R.string.sign_up));
    }
}