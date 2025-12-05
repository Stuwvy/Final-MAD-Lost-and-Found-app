// file: MainActivity.kt
package com.example.back2me

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.back2me.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recentAdapter: ItemGridAdapter
    private lateinit var oldAdapter: ItemListAdapter

    private val recentItems = mutableListOf<Item>()
    private val oldItems = mutableListOf<Item>()

    private val addItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadItems()
            Toast.makeText(this, "Item posted! List refreshed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupClickListeners()
        loadItems()
    }

    override fun onResume() {
        super.onResume()
        // Always select home when returning to this activity
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun setupRecyclerViews() {
        recentAdapter = ItemGridAdapter(recentItems) { item ->
            openItemDetail(item)
        }
        binding.recyclerRecent.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.recyclerRecent.adapter = recentAdapter

        oldAdapter = ItemListAdapter(oldItems) { item ->
            openItemDetail(item)
        }
        binding.recyclerOld.layoutManager = LinearLayoutManager(this)
        binding.recyclerOld.adapter = oldAdapter
    }

    private fun setupClickListeners() {
        binding.buttonSearch.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing
                    true
                }
                R.id.nav_add -> {
                    val intent = Intent(this, AddEditItemActivity::class.java)
                    addItemLauncher.launch(intent)
                    // Don't change selection, will reset in onResume
                    false
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    // Don't change selection, will reset in onResume
                    false
                }
                else -> false
            }
        }

        binding.textMoreRecent.setOnClickListener {
            Toast.makeText(this, "Show all recent items clicked!", Toast.LENGTH_SHORT).show()
        }
        binding.textMoreOld.setOnClickListener {
            Toast.makeText(this, "Show all older items clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadItems() {
        lifecycleScope.launch {
            try {
                val items = ItemRepository.getAllItems()
                Log.d("MainActivity", "Loaded ${items.size} items from Firestore")
                categorizeItems(items)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading items", e)
                Toast.makeText(this@MainActivity, "Error loading items: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun categorizeItems(items: List<Item>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = calendar.time

        recentItems.clear()
        oldItems.clear()

        items.forEach { item ->
            try {
                val itemDate = dateFormat.parse(item.createdDate)
                if (itemDate != null && itemDate.after(sevenDaysAgo)) {
                    recentItems.add(item)
                } else {
                    oldItems.add(item)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Date parsing error for item: ${item.id}", e)
                oldItems.add(item)
            }
        }

        Log.d("MainActivity", "Recent: ${recentItems.size}, Old: ${oldItems.size}")

        recentAdapter.notifyDataSetChanged()
        oldAdapter.notifyDataSetChanged()

        updateViewVisibility()
    }

    private fun updateViewVisibility() {
        val isRecentEmpty = recentItems.isEmpty()
        binding.textRecentTitle.visibility = if (isRecentEmpty) View.GONE else View.VISIBLE
        binding.textMoreRecent.visibility = if (isRecentEmpty) View.GONE else View.VISIBLE
        binding.recyclerRecent.visibility = if (isRecentEmpty) View.GONE else View.VISIBLE

        val isOldEmpty = oldItems.isEmpty()
        binding.textOldTitle.visibility = if (isOldEmpty) View.GONE else View.VISIBLE
        binding.textMoreOld.visibility = if (isOldEmpty) View.GONE else View.VISIBLE
        binding.recyclerOld.visibility = if (isOldEmpty) View.GONE else View.VISIBLE
    }

    private fun openItemDetail(item: Item) {
        val intent = Intent(this, ItemDetailActivity::class.java)
        intent.putExtra("ITEM_ID", item.id)
        startActivity(intent)
    }
}