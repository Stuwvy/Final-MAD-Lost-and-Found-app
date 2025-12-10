// file: SignupActivity.java
package com.example.back2me.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.back2me.AuthData;
import com.example.back2me.MainActivity;
import com.example.back2me.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        binding.backButton.setOnClickListener(v -> finish());

        binding.buttonSignup.setOnClickListener(v -> {
            String email = binding.inputEmail.getText().toString().trim();
            String password = binding.inputPassword.getText().toString().trim();
            String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();

            if (validateInput(email, password, confirmPassword)) {
                signupUser(email, password);
            }
        });

        binding.textLogin.setOnClickListener(v -> finish());

        // Social signup buttons (not implemented)
        binding.buttonGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google signup not implemented", Toast.LENGTH_SHORT).show()
        );

        binding.buttonFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Facebook signup not implemented", Toast.LENGTH_SHORT).show()
        );

        binding.buttonTwitter.setOnClickListener(v ->
                Toast.makeText(this, "Twitter signup not implemented", Toast.LENGTH_SHORT).show()
        );
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            binding.inputEmail.setError("Please enter your email");
            binding.inputEmail.requestFocus();
            return false;
        }

        // Basic email validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.setError("Please enter a valid email");
            binding.inputEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            binding.inputPassword.setError("Please enter your password");
            binding.inputPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            binding.inputPassword.setError("Password must be at least 6 characters");
            binding.inputPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            binding.inputConfirmPassword.setError("Please confirm your password");
            binding.inputConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.inputConfirmPassword.setError("Passwords do not match");
            binding.inputConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void signupUser(String email, String password) {
        showLoading(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Get the Firebase User ID and save it
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            AuthData.setUserId(user.getUid());
                        }

                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Signup failed";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.buttonSignup.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}