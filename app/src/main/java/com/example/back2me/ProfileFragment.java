package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.back2me.auth.LoginActivity;
import com.example.back2me.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setupUI();
        setupClickListeners();
    }

    private void setupUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            binding.textEmail.setText(email);

            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                binding.textName.setText(name);
                binding.textInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
            } else if (email != null && !email.isEmpty()) {
                binding.textName.setText(email.split("@")[0]);
                binding.textInitial.setText(String.valueOf(email.charAt(0)).toUpperCase());
            }
        }
    }

    private void setupClickListeners() {
        binding.cardMyItems.setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), MyItemsActivity.class)));

        binding.cardMyClaims.setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), MyClaimsActivity.class)));

        binding.cardMessages.setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), ConversationsActivity.class)));

        binding.cardSettings.setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), SettingsActivity.class)));

        binding.cardLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(requireContext(), R.string.logged_out, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
