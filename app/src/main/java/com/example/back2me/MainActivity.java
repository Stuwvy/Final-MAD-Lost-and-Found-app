package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivityMainBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private ItemGridAdapter recentAdapter;
    private ItemListAdapter oldAdapter;

    private final List<Item> recentItems = new ArrayList<>();
    private final List<Item> oldItems = new ArrayList<>();

    private final ActivityResultLauncher<Intent> addItemLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadItems();
                    Toast.makeText(this, "Item posted! List refreshed.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerViews();
        setupClickListeners();
        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always select home when returning to this activity
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        // Reload items in case something changed
        loadItems();
    }

    private void setupRecyclerViews() {
        recentAdapter = new ItemGridAdapter(recentItems, this::openItemDetail);
        binding.recyclerRecent.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.recyclerRecent.setAdapter(recentAdapter);

        oldAdapter = new ItemListAdapter(oldItems, this::openItemDetail);
        binding.recyclerOld.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerOld.setAdapter(oldAdapter);
    }

    private void setupClickListeners() {
        // ✅ Search button now opens SearchActivity
        binding.buttonSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });

        binding.bottomNavigation.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home, do nothing
                return true;
            } else if (itemId == R.id.nav_add) {
                Intent intent = new Intent(this, AddEditItemActivity.class);
                addItemLauncher.launch(intent);
                // Don't change selection, will reset in onResume
                return false;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                // Don't change selection, will reset in onResume
                return false;
            }

            return false;
        });

        binding.textMoreRecent.setOnClickListener(v -> {
            // Open search to see all items
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });

        binding.textMoreOld.setOnClickListener(v -> {
            // Open search to see all items
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private void loadItems() {
        ItemRepository.getAllItems(new ItemRepository.ItemsCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                Log.d(TAG, "Loaded " + items.size() + " items from Firestore");
                categorizeItems(items);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading items", e);
                Toast.makeText(MainActivity.this,
                        "Error loading items: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void categorizeItems(List<Item> items) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysAgo = calendar.getTime();

        recentItems.clear();
        oldItems.clear();

        for (Item item : items) {
            try {
                Date itemDate = isoFormat.parse(item.getCreatedDate());
                if (itemDate != null && itemDate.after(sevenDaysAgo)) {
                    recentItems.add(item);
                } else {
                    oldItems.add(item);
                }
            } catch (ParseException e) {
                Log.e(TAG, "Date parsing error for item: " + item.getId(), e);
                oldItems.add(item);
            }
        }

        Log.d(TAG, "Recent: " + recentItems.size() + ", Old: " + oldItems.size());

        recentAdapter.notifyDataSetChanged();
        oldAdapter.notifyDataSetChanged();

        updateViewVisibility();
    }

    private void updateViewVisibility() {
        boolean isRecentEmpty = recentItems.isEmpty();
        binding.textRecentTitle.setVisibility(isRecentEmpty ? View.GONE : View.VISIBLE);
        binding.textMoreRecent.setVisibility(isRecentEmpty ? View.GONE : View.VISIBLE);
        binding.recyclerRecent.setVisibility(isRecentEmpty ? View.GONE : View.VISIBLE);

        boolean isOldEmpty = oldItems.isEmpty();
        binding.textOldTitle.setVisibility(isOldEmpty ? View.GONE : View.VISIBLE);
        binding.textMoreOld.setVisibility(isOldEmpty ? View.GONE : View.VISIBLE);
        binding.recyclerOld.setVisibility(isOldEmpty ? View.GONE : View.VISIBLE);
    }

    private void openItemDetail(Item item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("ITEM_ID", item.getId());
        startActivity(intent);
    }
}