// file: SignupActivity.kt (CORRECTED)
package com.example.back2me.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.back2me.MainActivity
import com.example.back2me.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.back2me.AuthData // ✅ KEEP: Needed for setUserId

// ❌ REMOVE: import java.util.UUID (Not needed)

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.buttonSignup.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                signupUser(email, password)
            }
        }

        binding.textLogin.setOnClickListener {
            finish()
        }

        // Social signup buttons (not implemented)
        binding.buttonGoogle.setOnClickListener {
            Toast.makeText(this, "Google signup not implemented", Toast.LENGTH_SHORT).show()
        }
        binding.buttonFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook signup not implemented", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTwitter.setOnClickListener {
            Toast.makeText(this, "Twitter signup not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            binding.inputEmail.error = "Please enter your email"
            binding.inputEmail.requestFocus()
            return false
        }

        // Basic email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.error = "Please enter a valid email"
            binding.inputEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.inputPassword.error = "Please enter your password"
            binding.inputPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.inputPassword.error = "Password must be at least 6 characters"
            binding.inputPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.inputConfirmPassword.error = "Please confirm your password"
            binding.inputConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            binding.inputConfirmPassword.error = "Passwords do not match"
            binding.inputConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun signupUser(email: String, password: String) {
        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {

                    // 💥 FIX: Get the Firebase User ID and save it.
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        AuthData.setUserId(userId)
                    }

                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val errorMessage = task.exception?.message ?: "Signup failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonSignup.isEnabled = !show
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}