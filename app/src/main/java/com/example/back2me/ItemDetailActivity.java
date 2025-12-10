package com.example.back2me;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.back2me.databinding.ActivityItemDetailBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ItemDetailActivity extends AppCompatActivity {

    private static final String TAG = "ItemDetailActivity";
    private ActivityItemDetailBinding binding;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get item ID from intent
        itemId = getIntent().getStringExtra("ITEM_ID");

        setupClickListeners();
        loadItemDetails();
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.buttonClaim.setOnClickListener(v ->
                Toast.makeText(this, "Claim functionality coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadItemDetails() {
        if (itemId != null) {
            ItemRepository.getItemById(itemId).thenAccept(item -> {
                runOnUiThread(() -> {
                    if (item != null) {
                        displayItemData(item);
                    } else {
                        Toast.makeText(ItemDetailActivity.this,
                                "Item not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }).exceptionally(e -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading item", e);
                    Toast.makeText(ItemDetailActivity.this,
                            "Error loading item details", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return null;
            });
        } else {
            Toast.makeText(this, "Item ID not provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayItemData(Item item) {
        // Set item name
        binding.textItemName.setText(item.getName());

        // Set status
        String status = item.getStatus().toLowerCase();
        if (status.equals("lost")) {
            binding.textItemStatus.setText("Lost Item");
        } else if (status.equals("found")) {
            binding.textItemStatus.setText("Location found");
        } else {
            binding.textItemStatus.setText(item.getStatus());
        }

        // Format and set date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(item.getCreatedDate());

            if (date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                binding.textItemDate.setText(dateFormat.format(date));
                binding.textItemTime.setText(timeFormat.format(date));
            } else {
                binding.textItemDate.setText("N/A");
                binding.textItemTime.setText("N/A");
            }
        } catch (Exception e) {
            Log.e(TAG, "Date parsing error", e);
            binding.textItemDate.setText("N/A");
            binding.textItemTime.setText("N/A");
        }

        // Set description
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            binding.textItemDescription.setText(item.getDescription());
        } else {
            binding.textItemDescription.setText("No description provided");
        }

        // Load image if available
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(item.getImageUrl())
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .centerCrop()
                    .into(binding.imageItem);
        } else {
            // Set placeholder color if no image
            binding.imageItem.setBackgroundColor(getColor(R.color.lightBlue));
        }
    }
}