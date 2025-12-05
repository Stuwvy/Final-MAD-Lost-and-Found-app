package com.example.back2me

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.back2me.auth.LoginActivity
import com.example.back2me.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupUserInfo()
        setupClickListeners()
        loadUserStats()
    }

    private fun setupUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            binding.textUserName.text = user.displayName ?: "User"
            binding.textUserEmail.text = user.email ?: "No email"

            // Set initials for profile picture
            val initials = (user.displayName ?: user.email ?: "U")
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()
            binding.textProfileInitials.text = initials
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.cardMyItems.setOnClickListener {
            Toast.makeText(this, "My Items clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to MyItemsActivity
        }

        binding.cardSettings.setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to SettingsActivity
        }

        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }

        binding.cardHelp.setOnClickListener {
            showHelpDialog()
        }

        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserStats() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val allItems = ItemRepository.getAllItems()
                val myItems = allItems.filter { it.createdBy == userId }
                val lostItems = myItems.count { it.status == "lost" }
                val foundItems = myItems.count { it.status == "found" }

                binding.textTotalItems.text = myItems.size.toString()
                binding.textLostItems.text = lostItems.toString()
                binding.textFoundItems.text = foundItems.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Back2Me")
            .setMessage("""
                Back2Me - Lost & Found App
                
                Version: 1.0.0
                
                Help reunite people with their lost belongings and find items that have been found.
                
                © 2025 Back2Me
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Help & Support")
            .setMessage("""
                How to use Back2Me:
                
                📍 Post Lost Items:
                - Tap the + button
                - Select "Lost"
                - Add details and photo
                
                🔍 Post Found Items:
                - Tap the + button
                - Select "Found"
                - Add location and description
                
                Need more help?
                Contact: support@back2me.com
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}