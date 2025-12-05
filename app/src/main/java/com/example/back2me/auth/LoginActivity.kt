// file: LoginActivity.kt (UPDATED)
package com.example.back2me.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.back2me.MainActivity
import com.example.back2me.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.back2me.AuthData // ✅ KEEP: Needed for setUserId

// ❌ REMOVE: import java.util.UUID (Not needed for static DB GUID or user ID logic)

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.textSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Social login buttons (not implemented)
        binding.buttonGoogle.setOnClickListener {
            Toast.makeText(this, "Google login not implemented", Toast.LENGTH_SHORT).show()
        }
        binding.buttonFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook login not implemented", Toast.LENGTH_SHORT).show()
        }
        binding.buttonTwitter.setOnClickListener {
            Toast.makeText(this, "Twitter login not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
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

        return true
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {

                    // 💥 FIX: The DB GUID is static. We must save the authenticated User ID.
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        AuthData.setUserId(userId)
                    }

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !show
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}