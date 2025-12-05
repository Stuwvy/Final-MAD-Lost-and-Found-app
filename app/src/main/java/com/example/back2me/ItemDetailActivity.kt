package com.example.back2me

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.back2me.databinding.ActivityItemDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private var itemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get item ID from intent
        itemId = intent.getStringExtra("ITEM_ID")

        setupClickListeners()
        loadItemDetails()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.buttonClaim.setOnClickListener {
            // Handle claim item action
            Toast.makeText(this, "Claim functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadItemDetails() {
        itemId?.let { id ->
            lifecycleScope.launch {
                try {
                    val item = ItemRepository.getItemById(id)
                    if (item != null) {
                        displayItemData(item)
                    } else {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            "Item not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("ItemDetailActivity", "Error loading item", e)
                    Toast.makeText(
                        this@ItemDetailActivity,
                        "Error loading item details",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        } ?: run {
            Toast.makeText(this, "Item ID not provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayItemData(item: Item) {
        // Set item name
        binding.textItemName.text = item.name

        // Set status
        binding.textItemStatus.text = when (item.status.lowercase()) {
            "lost" -> "Lost Item"
            "found" -> "Location found"
            else -> item.status
        }

        // Format and set date
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(item.createdDate)

            if (date != null) {
                val dateFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                binding.textItemDate.text = dateFormat.format(date)
                binding.textItemTime.text = timeFormat.format(date)
            } else {
                binding.textItemDate.text = "N/A"
                binding.textItemTime.text = "N/A"
            }
        } catch (e: Exception) {
            Log.e("ItemDetailActivity", "Date parsing error", e)
            binding.textItemDate.text = "N/A"
            binding.textItemTime.text = "N/A"
        }

        // Set description
        binding.textItemDescription.text = if (item.description.isNotEmpty()) {
            item.description
        } else {
            "No description provided"
        }

        // Load image if available
        if (item.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(item.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .centerCrop()
                .into(binding.imageItem)
        } else {
            // Set placeholder color if no image
            binding.imageItem.setBackgroundColor(getColor(R.color.lightBlue))
        }
    }
}