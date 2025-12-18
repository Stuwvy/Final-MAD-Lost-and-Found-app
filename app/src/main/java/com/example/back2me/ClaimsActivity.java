package com.example.back2me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivityClaimsBinding;

import java.util.ArrayList;
import java.util.List;

public class ClaimsActivity extends AppCompatActivity implements ClaimsAdapter.OnClaimActionListener {

    private static final String TAG = "ClaimsActivity";

    private ActivityClaimsBinding binding;
    private ClaimsAdapter adapter;
    private List<Claim> claims = new ArrayList<>();
    private String itemId;
    private String itemName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClaimsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        itemId = getIntent().getStringExtra("ITEM_ID");
        itemName = getIntent().getStringExtra("ITEM_NAME");

        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Item ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupRecyclerView();
        loadClaims();
    }

    private void setupUI() {
        binding.backButton.setOnClickListener(v -> finish());

        if (itemName != null && !itemName.isEmpty()) {
            binding.textSubtitle.setText("Claims for: " + itemName);
            binding.textSubtitle.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new ClaimsAdapter(claims, this);
        binding.recyclerClaims.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerClaims.setAdapter(adapter);
    }

    private void loadClaims() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        ClaimRepository.getClaimsByItem(itemId, new ClaimRepository.ClaimsCallback() {
            @Override
            public void onSuccess(List<Claim> claimsList) {
                binding.progressBar.setVisibility(View.GONE);

                claims.clear();
                claims.addAll(claimsList);
                adapter.notifyDataSetChanged();

                updateEmptyState();

                Log.d(TAG, "Loaded " + claimsList.size() + " claims");
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading claims", e);
                Toast.makeText(ClaimsActivity.this,
                        "Error loading claims: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (claims.isEmpty()) {
            binding.recyclerClaims.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerClaims.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onApproveClick(Claim claim) {
        new AlertDialog.Builder(this)
                .setTitle("Approve Claim")
                .setMessage("Approve claim from " + claim.getClaimerName() + "?\n\nThis will mark the item as resolved.")
                .setPositiveButton("Approve", (dialog, which) -> approveClaim(claim))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRejectClick(Claim claim) {
        new AlertDialog.Builder(this)
                .setTitle("Reject Claim")
                .setMessage("Reject claim from " + claim.getClaimerName() + "?")
                .setPositiveButton("Reject", (dialog, which) -> rejectClaim(claim))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onContactClick(Claim claim) {
        new AlertDialog.Builder(this)
                .setTitle("Contact Claimer")
                .setMessage("Email: " + claim.getClaimerEmail() + "\n\nWould you like to send an email?")
                .setPositiveButton("Send Email", (dialog, which) -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + claim.getClaimerEmail()));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            "Regarding your claim for: " + claim.getItemName());

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send email via..."));
                    } catch (Exception e) {
                        Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveClaim(Claim claim) {
        binding.progressBar.setVisibility(View.VISIBLE);

        ClaimRepository.updateClaimStatus(claim.getId(), "approved",
                new ClaimRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        // Update item status to resolved
                        ItemRepository.getItemById(itemId, new ItemRepository.SingleItemCallback() {
                            @Override
                            public void onSuccess(Item item) {
                                item.setStatus("resolved");
                                ItemRepository.updateItem(item, new ItemRepository.UpdateCallback() {
                                    @Override
                                    public void onSuccess() {
                                        binding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ClaimsActivity.this,
                                                "Claim approved! Item marked as resolved.",
                                                Toast.LENGTH_SHORT).show();
                                        loadClaims();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ClaimsActivity.this,
                                                "Claim approved but failed to update item",
                                                Toast.LENGTH_SHORT).show();
                                        loadClaims();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(ClaimsActivity.this,
                                        "Claim approved!", Toast.LENGTH_SHORT).show();
                                loadClaims();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ClaimsActivity.this,
                                "Failed to approve claim: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void rejectClaim(Claim claim) {
        binding.progressBar.setVisibility(View.VISIBLE);

        ClaimRepository.updateClaimStatus(claim.getId(), "rejected",
                new ClaimRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ClaimsActivity.this, "Claim rejected", Toast.LENGTH_SHORT).show();
                        loadClaims();
                    }

                    @Override
                    public void onError(Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ClaimsActivity.this,
                                "Failed to reject claim: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}