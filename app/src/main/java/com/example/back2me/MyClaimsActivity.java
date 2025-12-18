package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivityMyClaimsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyClaimsActivity extends AppCompatActivity {

    private ActivityMyClaimsBinding binding;
    private FirebaseAuth auth;
    private MyClaimsAdapter adapter;
    private List<Claim> claimsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyClaimsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setupUI();
        loadMyClaims();
    }

    private void setupUI() {
        binding.backButton.setOnClickListener(v -> finish());

        adapter = new MyClaimsAdapter(claimsList, claim -> {
            // Open item detail when claim is clicked
            Intent intent = new Intent(this, ItemDetailActivity.class);
            intent.putExtra("ITEM_ID", claim.getItemId());
            startActivity(intent);
        });

        binding.recyclerClaims.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerClaims.setAdapter(adapter);
    }

    private void loadMyClaims() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        ClaimRepository.getClaimsByClaimer(currentUser.getUid(), new ClaimRepository.ClaimsCallback() {
            @Override
            public void onSuccess(List<Claim> claims) {
                binding.progressBar.setVisibility(View.GONE);
                claimsList.clear();
                claimsList.addAll(claims);
                adapter.notifyDataSetChanged();

                if (claims.isEmpty()) {
                    binding.emptyState.setVisibility(View.VISIBLE);
                    binding.recyclerClaims.setVisibility(View.GONE);
                } else {
                    binding.emptyState.setVisibility(View.GONE);
                    binding.recyclerClaims.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyClaimsActivity.this, 
                        "Error loading claims: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyClaims();
    }
}
