package com.example.back2me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.back2me.databinding.ActivityItemDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ItemDetailActivity extends AppCompatActivity {

    private static final String TAG = "ItemDetailActivity";

    private ActivityItemDetailBinding binding;
    private String itemId;
    private Item currentItem;
    private FirebaseAuth auth;
    private boolean isOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // Get item ID from intent
        itemId = getIntent().getStringExtra("ITEM_ID");

        setupClickListeners();
        loadItemDetails();
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        // Contact Owner button
        binding.buttonContact.setOnClickListener(v -> contactOwner());

        // Claim Item button
        binding.buttonClaim.setOnClickListener(v -> showClaimDialog());

        // View Claims button (only for owner)
        binding.buttonViewClaims.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClaimsActivity.class);
            intent.putExtra("ITEM_ID", itemId);
            intent.putExtra("ITEM_NAME", currentItem != null ? currentItem.getName() : "");
            startActivity(intent);
        });
    }

    private void loadItemDetails() {
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Item ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        ItemRepository.getItemById(itemId, new ItemRepository.ItemCallback() {
            @Override
            public void onSuccess(Item item) {
                binding.progressBar.setVisibility(View.GONE);

                if (item != null) {
                    currentItem = item;
                    displayItemData(item);
                    checkOwnership(item);
                    checkExistingClaim();
                } else {
                    Toast.makeText(ItemDetailActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading item", e);
                Toast.makeText(ItemDetailActivity.this,
                        "Error loading item details", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void checkOwnership(Item item) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && item.getCreatedBy().equals(currentUser.getUid())) {
            isOwner = true;
            // Show View Claims button, hide Contact/Claim buttons
            binding.buttonViewClaims.setVisibility(View.VISIBLE);
            binding.buttonContact.setVisibility(View.GONE);
            binding.buttonClaim.setVisibility(View.GONE);
            binding.textOwnerNote.setVisibility(View.VISIBLE);
            binding.textOwnerNote.setText("This is your item. View claims below.");
        } else {
            isOwner = false;
            // Show Contact/Claim buttons, hide View Claims
            binding.buttonViewClaims.setVisibility(View.GONE);
            binding.buttonContact.setVisibility(View.VISIBLE);
            binding.buttonClaim.setVisibility(View.VISIBLE);
            binding.textOwnerNote.setVisibility(View.GONE);

            // Update button text based on item status
            if ("lost".equals(item.getStatus().toLowerCase())) {
                binding.buttonClaim.setText("✋ I Found This Item!");
            } else {
                binding.buttonClaim.setText("🙋 This Is Mine!");
            }
        }
    }

    private void checkExistingClaim() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || isOwner) return;

        ClaimRepository.hasUserClaimedItem(itemId, currentUser.getUid(),
                new ClaimRepository.ExistsCallback() {
                    @Override
                    public void onResult(boolean exists) {
                        if (exists) {
                            binding.buttonClaim.setEnabled(false);
                            binding.buttonClaim.setText("✅ Already Claimed");
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error checking existing claim", e);
                    }
                });
    }

    private void displayItemData(Item item) {
        // Set item name
        binding.textItemName.setText(item.getName());

        // Set status badge
        String status = item.getStatus().toLowerCase();
        if (status.equals("lost")) {
            binding.textItemStatus.setText("🔴 Lost Item");
            binding.textItemStatus.setBackgroundResource(R.drawable.badge_lost);
        } else if (status.equals("found")) {
            binding.textItemStatus.setText("🟢 Found Item");
            binding.textItemStatus.setBackgroundResource(R.drawable.badge_found);
        } else if (status.equals("resolved")) {
            binding.textItemStatus.setText("✅ Resolved");
            binding.textItemStatus.setBackgroundResource(R.drawable.badge_resolved);
            // Hide action buttons if resolved
            binding.buttonContact.setVisibility(View.GONE);
            binding.buttonClaim.setVisibility(View.GONE);
        } else {
            binding.textItemStatus.setText(item.getStatus());
        }

        // Set location
        binding.textItemLocation.setText(item.getLocation());

        // Format and set date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(item.getCreatedDate());

            if (date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                binding.textItemDate.setText(dateFormat.format(date));
                binding.textItemTime.setText(timeFormat.format(date));
            } else {
                binding.textItemDate.setText("N/A");
                binding.textItemTime.setText("N/A");
            }
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing error", e);
            binding.textItemDate.setText("N/A");
            binding.textItemTime.setText("N/A");
        }

        // Set description
        String description = item.getDescription();
        if (description != null && !description.isEmpty()) {
            binding.textItemDescription.setText(description);
        } else {
            binding.textItemDescription.setText("No description provided");
        }

        // Load image if available
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(binding.imageItem);
        } else {
            binding.imageItem.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void contactOwner() {
        if (currentItem == null) return;

        String subject = "Regarding your " + currentItem.getStatus() + " item: " + currentItem.getName();
        String body = "Hi,\n\nI saw your " + currentItem.getStatus() + " item listing for \""
                + currentItem.getName() + "\" on Back2Me.\n\n";

        if ("lost".equals(currentItem.getStatus().toLowerCase())) {
            body += "I believe I may have found your item.\n\n";
        } else {
            body += "I believe this might be my item.\n\n";
        }
        body += "Please let me know how we can connect.\n\nThank you!";

        final String emailBody = body;

        // Show dialog with options
        new AlertDialog.Builder(this)
                .setTitle("Contact Owner")
                .setMessage("How would you like to contact the owner?")
                .setPositiveButton("💬 In-App Message", (dialog, which) -> openChat())
                .setNeutralButton("📧 Email", (dialog, which) -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send email via..."));
                    } catch (Exception e) {
                        Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openChat() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || currentItem == null) {
            Toast.makeText(this, "Please login to send messages", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get owner info - in a real app you'd fetch this from a users collection
        // For now, we use the createdBy field as ID
        String ownerId = currentItem.getCreatedBy();
        String ownerName = "Item Owner"; // Would fetch from users collection

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("OTHER_USER_ID", ownerId);
        intent.putExtra("OTHER_USER_NAME", ownerName);
        intent.putExtra("ITEM_ID", itemId);
        intent.putExtra("ITEM_NAME", currentItem.getName());
        startActivity(intent);
    }

    private void showClaimDialog() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to claim items", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentItem == null) return;

        // Create input field for message
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setHint("Describe why you believe this is yours or how you found it...");
        input.setPadding(48, 32, 48, 32);

        String title, message;
        if ("lost".equals(currentItem.getStatus().toLowerCase())) {
            title = "I Found This Item";
            message = "Please describe where and how you found \"" + currentItem.getName() + "\":";
        } else {
            title = "Claim This Item";
            message = "Please describe why you believe \"" + currentItem.getName() + "\" belongs to you:";
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setView(input)
                .setPositiveButton("Submit Claim", (dialog, which) -> {
                    String claimMessage = input.getText().toString().trim();
                    if (claimMessage.isEmpty()) {
                        Toast.makeText(this, "Please provide a description", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitClaim(claimMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitClaim(String message) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || currentItem == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        // Get current date in ISO format
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String createdDate = isoFormat.format(new Date());

        String claimerName = currentUser.getDisplayName();
        if (claimerName == null || claimerName.isEmpty()) {
            claimerName = currentUser.getEmail();
        }

        Claim claim = new Claim(
                itemId,
                currentItem.getName(),
                currentUser.getUid(),
                currentUser.getEmail() != null ? currentUser.getEmail() : "",
                claimerName != null ? claimerName : "Anonymous",
                currentItem.getCreatedBy(),
                message,
                createdDate,
                currentItem.getStatus()
        );

        ClaimRepository.createClaim(claim, new ClaimRepository.CreateCallback() {
            @Override
            public void onSuccess(Claim claim) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ItemDetailActivity.this,
                        "Claim submitted successfully!", Toast.LENGTH_SHORT).show();

                // Update button state
                binding.buttonClaim.setEnabled(false);
                binding.buttonClaim.setText("✅ Already Claimed");
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ItemDetailActivity.this,
                        "Failed to submit claim: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}