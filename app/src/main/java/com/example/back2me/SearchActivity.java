package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivitySearchBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private ItemListAdapter searchAdapter;

    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private String currentFilter = "all"; // "all", "lost", "found"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupClickListeners();
        setupSearchInput();
        loadAllItems();
    }

    private void setupRecyclerView() {
        searchAdapter = new ItemListAdapter(filteredItems, this::openItemDetail);
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSearchResults.setAdapter(searchAdapter);
    }

    private void setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener(v -> finish());

        // Clear search button
        binding.buttonClearSearch.setOnClickListener(v -> {
            binding.inputSearch.setText("");
            binding.inputSearch.clearFocus();
        });

        // Filter chip group
        binding.chipAll.setOnClickListener(v -> setFilter("all"));
        binding.chipLost.setOnClickListener(v -> setFilter("lost"));
        binding.chipFound.setOnClickListener(v -> setFilter("found"));
    }

    private void setupSearchInput() {
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button
                binding.buttonClearSearch.setVisibility(
                        s == null || s.length() == 0 ? View.GONE : View.VISIBLE
                );

                // Apply filter
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setFilter(String filter) {
        currentFilter = filter;

        // Update chip selection states
        binding.chipAll.setChecked(filter.equals("all"));
        binding.chipLost.setChecked(filter.equals("lost"));
        binding.chipFound.setChecked(filter.equals("found"));

        applyFilters();
    }

    private void loadAllItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        ItemRepository.getAllItems(new ItemRepository.ItemsCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                allItems.clear();
                allItems.addAll(items);

                applyFilters();

                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(
                        SearchActivity.this,
                        "Error loading items: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void applyFilters() {
        String searchQuery = binding.inputSearch.getText().toString().trim().toLowerCase();

        filteredItems.clear();

        for (Item item : allItems) {
            // Check status filter
            boolean matchesStatus;
            switch (currentFilter) {
                case "lost":
                    matchesStatus = item.getStatus().toLowerCase().equals("lost");
                    break;
                case "found":
                    matchesStatus = item.getStatus().toLowerCase().equals("found");
                    break;
                default:
                    matchesStatus = true; // "all"
                    break;
            }

            // Check search query
            boolean matchesSearch;
            if (searchQuery.isEmpty()) {
                matchesSearch = true;
            } else {
                matchesSearch = item.getName().toLowerCase().contains(searchQuery) ||
                        item.getLocation().toLowerCase().contains(searchQuery) ||
                        item.getDescription().toLowerCase().contains(searchQuery);
            }

            if (matchesStatus && matchesSearch) {
                filteredItems.add(item);
            }
        }

        // Update UI
        searchAdapter.notifyDataSetChanged();
        updateEmptyState();
        updateResultCount();
    }

    private void updateEmptyState() {
        if (filteredItems.isEmpty()) {
            binding.recyclerSearchResults.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);

            // Update empty state message based on context
            String searchQuery = binding.inputSearch.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                binding.textEmptyMessage.setText("No items found for \"" + searchQuery + "\"");
            } else if (!currentFilter.equals("all")) {
                binding.textEmptyMessage.setText("No " + currentFilter + " items found");
            } else {
                binding.textEmptyMessage.setText("No items available");
            }
        } else {
            binding.recyclerSearchResults.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void updateResultCount() {
        int count = filteredItems.size();
        String text;
        if (count == 0) {
            text = "No results";
        } else if (count == 1) {
            text = "1 item found";
        } else {
            text = count + " items found";
        }
        binding.textResultCount.setText(text);
        binding.textResultCount.setVisibility(View.VISIBLE);
    }

    private void openItemDetail(Item item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("ITEM_ID", item.getId());
        startActivity(intent);
    }
}