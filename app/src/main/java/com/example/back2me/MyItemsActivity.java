package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivityMyItemsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyItemsActivity extends AppCompatActivity implements MyItemsAdapter.OnItemActionListener {

    private ActivityMyItemsBinding binding;
    private FirebaseAuth auth;
    private MyItemsAdapter adapter;
    private List<Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupUI();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void setupUI() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MyItemsAdapter(items, this);
        binding.recyclerMyItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMyItems.setAdapter(adapter);
    }

    private void loadItems() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        ItemRepository.getItemsByUser(user.getUid(), new ItemRepository.ItemsCallback() {
            @Override
            public void onSuccess(List<Item> itemList) {
                binding.progressBar.setVisibility(View.GONE);
                items.clear();
                items.addAll(itemList);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyItemsActivity.this,
                        "Error loading items: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (items.isEmpty()) {
            binding.recyclerMyItems.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerMyItems.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

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
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Item item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_item)
                .setMessage(R.string.delete_item_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onViewClaimsClick(Item item) {
        Intent intent = new Intent(this, ClaimsActivity.class);
        intent.putExtra("ITEM_ID", item.getId());
        intent.putExtra("ITEM_NAME", item.getName());
        startActivity(intent);
    }

    @Override
    public void onMarkResolvedClick(Item item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.mark_resolved)
                .setMessage(R.string.mark_resolved_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> markAsResolved(item))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteItem(Item item) {
        binding.progressBar.setVisibility(View.VISIBLE);

        ItemRepository.deleteItem(item.getId(), new ItemRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyItemsActivity.this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                loadItems();
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyItemsActivity.this,
                        "Error deleting item: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void markAsResolved(Item item) {
        binding.progressBar.setVisibility(View.VISIBLE);

        item.setStatus("resolved");

        ItemRepository.updateItem(item, new ItemRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyItemsActivity.this, R.string.item_marked_resolved, Toast.LENGTH_SHORT).show();
                loadItems();
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyItemsActivity.this,
                        "Error updating item: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}