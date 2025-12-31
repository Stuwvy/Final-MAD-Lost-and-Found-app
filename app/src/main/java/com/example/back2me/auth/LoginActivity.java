package com.example.back2me.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.back2me.MainActivity;
import com.example.back2me.R;
import com.example.back2me.databinding.ActivityLoginBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v -> finish());

        // Login button
        binding.buttonLogin.setOnClickListener(v -> attemptLogin());

        // Sign up link
        binding.textSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        // Forgot password
        binding.textForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void attemptLogin() {
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

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
        binding.layoutPassword.setError(null);

        // Show loading
        setLoading(true);

        // Attempt login
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText inputEmail = dialogView.findViewById(R.id.input_email);

        // Pre-fill with current email if entered
        String currentEmail = binding.inputEmail.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            inputEmail.setText(currentEmail);
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your email address and we'll send you a link to reset your password.")
                .setView(dialogView)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = inputEmail.getText().toString().trim();
                    if (!TextUtils.isEmpty(email)) {
                        sendPasswordResetEmail(email);
                    } else {
                        Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Password reset email sent to " + email,
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to send reset email: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!loading);
        binding.buttonLogin.setText(loading ? "" : getString(R.string.sign_in));
    }
}