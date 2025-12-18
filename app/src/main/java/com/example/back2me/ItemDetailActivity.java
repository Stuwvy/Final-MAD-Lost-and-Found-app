package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.back2me.databinding.ActivityItemDetailBinding;
import com.google.android.material.textfield.TextInputEditText;
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
    private FirebaseAuth auth;
    private String itemId;
    private Item currentItem;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        currentUserId = user != null ? user.getUid() : "";

        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClickListeners();
        loadItem();
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.buttonClaim.setOnClickListener(v -> showClaimDialog());
        binding.buttonContact.setOnClickListener(v -> startChat());
        binding.buttonViewClaims.setOnClickListener(v -> openClaimsActivity());
    }

    private void loadItem() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ItemRepository.getItemById(itemId, new ItemRepository.SingleItemCallback() {
            @Override
            public void onSuccess(Item item) {
                binding.progressBar.setVisibility(View.GONE);
                currentItem = item;
                displayItem(item);
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading item", e);
                Toast.makeText(ItemDetailActivity.this,
                        "Error loading item: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayItem(Item item) {
        binding.textItemName.setText(item.getName());
        binding.textLocation.setText(item.getLocation());
        binding.textDescription.setText(item.getDescription());

        // Status badge
        String status = item.getStatus();
        if ("lost".equalsIgnoreCase(status)) {
            binding.textStatus.setText(R.string.lost);
            binding.textStatus.setBackgroundResource(R.drawable.badge_lost);
        } else if ("found".equalsIgnoreCase(status)) {
            binding.textStatus.setText(R.string.found);
            binding.textStatus.setBackgroundResource(R.drawable.badge_found);
        } else {
            binding.textStatus.setText(R.string.resolved);
            binding.textStatus.setBackgroundResource(R.drawable.badge_resolved);
        }

        // Date
        String formattedDate = formatDate(item.getCreatedDate());
        binding.textDate.setText(formattedDate);

        // Image
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

        // Show/hide buttons based on ownership
        boolean isOwner = currentUserId.equals(item.getCreatedBy());
        if (isOwner) {
            binding.buttonClaim.setVisibility(View.GONE);
            binding.buttonContact.setVisibility(View.GONE);
            binding.buttonViewClaims.setVisibility(View.VISIBLE);
        } else {
            binding.buttonClaim.setVisibility(View.VISIBLE);
            binding.buttonContact.setVisibility(View.VISIBLE);
            binding.buttonViewClaims.setVisibility(View.GONE);
        }

        // Hide claim button if resolved
        if ("resolved".equalsIgnoreCase(status)) {
            binding.buttonClaim.setVisibility(View.GONE);
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";

        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoDate);

            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            return displayFormat.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    private void showClaimDialog() {
        if (currentItem == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_claim_item, null);
        TextInputEditText editMessage = dialogView.findViewById(R.id.edit_claim_message);

        new AlertDialog.Builder(this)
                .setTitle(R.string.claim_item)
                .setView(dialogView)
                .setPositiveButton(R.string.submit, (dialog, which) -> {
                    String message = editMessage.getText() != null ?
                            editMessage.getText().toString().trim() : "";
                    if (message.isEmpty()) {
                        Toast.makeText(this, R.string.please_enter_message, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitClaim(message);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitClaim(String message) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        Claim claim = new Claim();
        claim.setItemId(itemId);
        claim.setItemName(currentItem.getName());
        claim.setItemStatus(currentItem.getStatus());
        claim.setClaimerId(user.getUid());
        claim.setClaimerName(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
        claim.setClaimerEmail(user.getEmail());
        claim.setOwnerId(currentItem.getCreatedBy());
        claim.setMessage(message);
        claim.setStatus("pending");

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        claim.setCreatedDate(isoFormat.format(new Date()));

        ClaimRepository.createClaim(claim, new ClaimRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ItemDetailActivity.this,
                        R.string.claim_submitted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ItemDetailActivity.this,
                        "Failed to submit claim: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startChat() {
        if (currentItem == null) return;

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = currentItem.getCreatedBy();
        if (ownerId.equals(user.getUid())) {
            Toast.makeText(this, R.string.this_is_your_item, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        ChatRepository.getOrCreateConversation(
                user.getUid(),
                user.getEmail(),
                ownerId,
                "Item Owner",
                itemId,
                currentItem.getName(),
                new ChatRepository.ConversationCallback() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        binding.progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(ItemDetailActivity.this, ChatActivity.class);
                        intent.putExtra("CONVERSATION_ID", conversation.getId());
                        intent.putExtra("OTHER_USER_NAME", "Item Owner");
                        intent.putExtra("ITEM_NAME", currentItem.getName());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ItemDetailActivity.this,
                                "Failed to start chat: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openClaimsActivity() {
        if (currentItem == null) return;

        Intent intent = new Intent(this, ClaimsActivity.class);
        intent.putExtra("ITEM_ID", itemId);
        intent.putExtra("ITEM_NAME", currentItem.getName());
        startActivity(intent);
    }
}