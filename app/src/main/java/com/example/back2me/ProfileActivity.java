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

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupUserInfo();
        setupClickListeners();
        loadUserStats();
    }

    private void setupUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            binding.textUserName.setText(displayName != null ? displayName : "User");
            binding.textUserEmail.setText(email != null ? email : "No email");

            // Set initials for profile picture
            String source = displayName != null ? displayName : (email != null ? email : "U");
            StringBuilder initials = new StringBuilder();
            String[] parts = source.split(" ");
            for (String part : parts) {
                if (!part.isEmpty() && initials.length() < 2) {
                    initials.append(Character.toUpperCase(part.charAt(0)));
                }
            }
            binding.textProfileInitials.setText(initials.toString());
        }
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.cardMyItems.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyItemsActivity.class);
            startActivity(intent);
        });

        binding.cardMessages.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConversationsActivity.class);
            startActivity(intent);
        });

        binding.cardSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to SettingsActivity
        });

        binding.cardAbout.setOnClickListener(v -> showAboutDialog());

        binding.cardHelp.setOnClickListener(v -> showHelpDialog());

        binding.buttonLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadUserStats() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        ItemRepository.getAllItems(new ItemRepository.ItemsCallback() {
            @Override
            public void onSuccess(List<Item> allItems) {
                int totalItems = 0;
                int lostItems = 0;
                int foundItems = 0;

                for (Item item : allItems) {
                    if (userId.equals(item.getCreatedBy())) {
                        totalItems++;
                        if ("lost".equals(item.getStatus())) {
                            lostItems++;
                        } else if ("found".equals(item.getStatus())) {
                            foundItems++;
                        }
                    }
                }

                binding.textTotalItems.setText(String.valueOf(totalItems));
                binding.textLostItems.setText(String.valueOf(lostItems));
                binding.textFoundItems.setText(String.valueOf(foundItems));
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                // Set default values on error
                binding.textTotalItems.setText("0");
                binding.textLostItems.setText("0");
                binding.textFoundItems.setText("0");
            }
        });
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About Back2Me")
                .setMessage("Back2Me - Lost & Found App\n\n" +
                        "Version: 1.0.0\n\n" +
                        "Help reunite people with their lost belongings and find items that have been found.\n\n" +
                        "© 2025 Back2Me")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help & Support")
                .setMessage("How to use Back2Me:\n\n" +
                        "🔍 Post Lost Items:\n" +
                        "- Tap the + button\n" +
                        "- Select \"Lost\"\n" +
                        "- Add details and photo\n\n" +
                        "📍 Post Found Items:\n" +
                        "- Tap the + button\n" +
                        "- Select \"Found\"\n" +
                        "- Add location and description\n\n" +
                        "Need more help?\n" +
                        "Contact: support@back2me.com")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        auth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}