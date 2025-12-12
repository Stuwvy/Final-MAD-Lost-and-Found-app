package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivityMyItemsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyItemsActivity extends AppCompatActivity implements MyItemsAdapter.OnItemActionListener {

    private static final String TAG = "MyItemsActivity";

    private ActivityMyItemsBinding binding;
    private MyItemsAdapter adapter;
    private List<Item> myItems = new ArrayList<>();
    private FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> editItemLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadMyItems(); // Reload after edit
                    Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        setupClickListeners();
        loadMyItems();
    }

    private void setupRecyclerView() {
        adapter = new MyItemsAdapter(myItems, this);
        binding.recyclerMyItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMyItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditItemActivity.class);
            editItemLauncher.launch(intent);
        });
    }

    private void loadMyItems() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.recyclerMyItems.setVisibility(View.GONE);

        ItemRepository.getItemsByUser(userId, new ItemRepository.ItemsCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                binding.progressBar.setVisibility(View.GONE);

                myItems.clear();
                myItems.addAll(items);
                adapter.notifyDataSetChanged();

                updateEmptyState();

                Log.d(TAG, "Loaded " + items.size() + " items for user");
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading items", e);
                Toast.makeText(MyItemsActivity.this,
                        "Error loading items: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (myItems.isEmpty()) {
            binding.recyclerMyItems.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerMyItems.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    // ============== Item Action Callbacks ==============

    @Override
    public void onItemClick(Item item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("ITEM_ID", item.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Item item) {
        Intent intent = new Intent(this, EditItemActivity.class);
        intent.putExtra("ITEM_ID", item.getId());
        editItemLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMarkResolvedClick(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Resolved")
                .setMessage("Mark \"" + item.getName() + "\" as resolved/returned?")
                .setPositiveButton("Yes", (dialog, which) -> markAsResolved(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(Item item) {
        ItemRepository.deleteItem(item.getId(), new ItemRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MyItemsActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                loadMyItems(); // Reload list
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MyItemsActivity.this,
                        "Failed to delete: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void markAsResolved(Item item) {
        // Create updated item with "resolved" status
        Item updatedItem = new Item(
                item.getId(),
                item.getName(),
                item.getLocation(),
                item.getDescription(),
                "resolved",  // Change status to resolved
                item.getCreatedBy(),
                item.getCreatedDate(),
                item.getImageUrl()
        );

        ItemRepository.updateItem(item.getId(), updatedItem, new ItemRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MyItemsActivity.this, "Item marked as resolved!", Toast.LENGTH_SHORT).show();
                loadMyItems(); // Reload list
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MyItemsActivity.this,
                        "Failed to update: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}