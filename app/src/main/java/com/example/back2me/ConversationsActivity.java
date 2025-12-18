package com.example.back2me;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivityConversationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ConversationsActivity extends AppCompatActivity implements ConversationsAdapter.OnConversationClickListener {

    private static final String TAG = "ConversationsActivity";

    private ActivityConversationsBinding binding;
    private ConversationsAdapter adapter;
    private List<Conversation> conversations = new ArrayList<>();

    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = user.getUid();

        setupUI();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void setupUI() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ConversationsAdapter(conversations, currentUserId, this);
        binding.recyclerConversations.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerConversations.setAdapter(adapter);
    }

    private void loadConversations() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        ChatRepository.getConversationsForUser(currentUserId, new ChatRepository.ConversationsCallback() {
            @Override
            public void onSuccess(List<Conversation> conversationsList) {
                binding.progressBar.setVisibility(View.GONE);

                conversations.clear();
                conversations.addAll(conversationsList);
                adapter.notifyDataSetChanged();

                updateEmptyState();

                Log.d(TAG, "Loaded " + conversationsList.size() + " conversations");
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading conversations", e);
                Toast.makeText(ConversationsActivity.this,
                        R.string.error_loading_conversations,
                        Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            binding.recyclerConversations.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerConversations.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", conversation.getId());
        intent.putExtra("OTHER_USER_NAME", conversation.getOtherUserName(currentUserId));
        intent.putExtra("OTHER_USER_ID", conversation.getOtherUserId(currentUserId));
        intent.putExtra("ITEM_NAME", conversation.getItemName());
        intent.putExtra("ITEM_ID", conversation.getItemId());
        startActivity(intent);
    }
}