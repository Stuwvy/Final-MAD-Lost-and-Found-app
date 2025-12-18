package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.back2me.auth.LoginActivity;
import com.example.back2me.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupUI();
        setupClickListeners();
        loadUserStats();
    }

    private void setupUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            binding.textEmail.setText(email);

            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                binding.textName.setText(name);
            } else if (email != null) {
                binding.textName.setText(email.split("@")[0]);
            }
        }
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v -> finish());

        // My Items - Opens MyItemsActivity
        binding.cardMyItems.setOnClickListener(v -> {
            startActivity(new Intent(this, MyItemsActivity.class));
        });

        // My Claims - Opens MyClaimsActivity
        binding.cardMyClaims.setOnClickListener(v -> {
            startActivity(new Intent(this, MyClaimsActivity.class));
        });

        // Messages - Opens ConversationsActivity
        binding.cardMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, ConversationsActivity.class));
        });

        // Settings - Opens SettingsActivity
        binding.cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // Help & Support
        binding.cardHelp.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Help & Support")
                    .setMessage("For help, please contact:\n\nsupport@back2me.com\n\nOr visit our FAQ section on the website.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        // About
        binding.cardAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("About Back2Me")
                    .setMessage("Back2Me v1.0\n\nA Lost & Found app to help reunite people with their lost items.\n\nDeveloped for MAD Final Project 2025")
                    .setPositiveButton("OK", null)
                    .show();
        });

        // Logout
        binding.buttonLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        auth.signOut();
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadUserStats() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // Load user's items count
        ItemRepository.getItemsByUser(userId, new ItemRepository.ItemsCallback() {
            @Override
            public void onSuccess(java.util.List<Item> items) {
                int total = items.size();
                int lost = 0;
                int found = 0;

                for (Item item : items) {
                    if ("lost".equalsIgnoreCase(item.getStatus())) {
                        lost++;
                    } else if ("found".equalsIgnoreCase(item.getStatus())) {
                        found++;
                    }
                }

                binding.textTotalItems.setText(String.valueOf(total));
                binding.textLostItems.setText(String.valueOf(lost));
                binding.textFoundItems.setText(String.valueOf(found));
            }

            @Override
            public void onError(Exception e) {
                binding.textTotalItems.setText("0");
                binding.textLostItems.setText("0");
                binding.textFoundItems.setText("0");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserStats();
    }
}