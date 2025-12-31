package com.example.back2me;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.back2me.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private ActivityChatBinding binding;
    private MessagesAdapter adapter;
    private List<Message> messages = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserName;
    private String conversationId;
    private String otherUserName;
    private String otherUserId;
    private String itemName;
    private String itemId;

    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        currentUserName = currentUser.getDisplayName();
        if (currentUserName == null || currentUserName.isEmpty()) {
            currentUserName = currentUser.getEmail();
        }

        // Get data from intent
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");
        otherUserId = getIntent().getStringExtra("OTHER_USER_ID");
        itemName = getIntent().getStringExtra("ITEM_NAME");
        itemId = getIntent().getStringExtra("ITEM_ID");

        if (conversationId == null || conversationId.isEmpty()) {
            // Need to create a new conversation
            createNewConversation();
        } else {
            setupUI();
            setupRecyclerView();
            startListeningToMessages();

            // Look up real user name
            lookupUserName();
        }
    }

    private void lookupUserName() {
        if (otherUserId == null || otherUserId.isEmpty()) return;

        // First try users collection
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String displayName = document.getString("displayName");
                        if (displayName != null && !displayName.isEmpty()) {
                            otherUserName = displayName;
                            updateHeaderUI();
                            return;
                        }
                    }
                    // If not found in users, try to get from messages
                    lookupNameFromMessages();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error looking up user", e);
                    lookupNameFromMessages();
                });
    }

    private void lookupNameFromMessages() {
        if (conversationId == null || otherUserId == null) return;

        // Get messages and find sender name for otherUserId
        db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .whereEqualTo("senderId", otherUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String senderName = querySnapshot.getDocuments().get(0).getString("senderName");
                        if (senderName != null && !senderName.isEmpty() && !senderName.equals("Item Owner")) {
                            otherUserName = senderName;
                            updateHeaderUI();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error looking up name from messages", e));
    }

    private void updateHeaderUI() {
        binding.textUserName.setText(otherUserName != null ? otherUserName : "User");

        // Update avatar initial
        if (otherUserName != null && !otherUserName.isEmpty()) {
            String initial;
            // If it's an email, get first letter before @
            if (otherUserName.contains("@")) {
                initial = otherUserName.substring(0, 1).toUpperCase();
            } else {
                initial = String.valueOf(otherUserName.charAt(0)).toUpperCase();
            }
            binding.textAvatar.setText(initial);
        } else {
            binding.textAvatar.setText("U");
        }
    }

    private void createNewConversation() {
        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "Cannot start conversation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        // Look up real name before creating conversation
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String displayName = document.getString("displayName");
                        if (displayName != null && !displayName.isEmpty()) {
                            otherUserName = displayName;
                        }
                    }
                    doCreateConversation();
                })
                .addOnFailureListener(e -> doCreateConversation());
    }

    private void doCreateConversation() {
        ChatRepository.getOrCreateConversation(
                currentUserId, currentUserName,
                otherUserId, otherUserName != null ? otherUserName : "User",
                itemId != null ? itemId : "",
                itemName != null ? itemName : "",
                new ChatRepository.ConversationCallback() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        binding.progressBar.setVisibility(View.GONE);
                        conversationId = conversation.getId();
                        setupUI();
                        setupRecyclerView();
                        startListeningToMessages();
                    }

                    @Override
                    public void onError(Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error creating conversation", e);
                        Toast.makeText(ChatActivity.this,
                                R.string.error_creating_conversation,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListeningToMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (conversationId != null && !conversationId.isEmpty()) {
            startListeningToMessages();
        }
    }

    private void setupUI() {
        binding.backButton.setOnClickListener(v -> finish());

        // Set header info
        binding.textUserName.setText(otherUserName != null ? otherUserName : "User");

        if (itemName != null && !itemName.isEmpty()) {
            binding.textItemName.setText(getString(R.string.regarding_item, itemName));
            binding.textItemName.setVisibility(View.VISIBLE);
        } else {
            binding.textItemName.setVisibility(View.GONE);
        }

        // Set avatar initial
        if (otherUserName != null && !otherUserName.isEmpty()) {
            binding.textAvatar.setText(String.valueOf(otherUserName.charAt(0)).toUpperCase());
        } else {
            binding.textAvatar.setText("U");
        }

        // Send button state
        binding.buttonSend.setEnabled(false);
        binding.buttonSend.setAlpha(0.5f);

        // Text watcher for input
        binding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && s.toString().trim().length() > 0;
                binding.buttonSend.setEnabled(hasText);
                binding.buttonSend.setAlpha(hasText ? 1.0f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Send button click
        binding.buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        adapter = new MessagesAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(adapter);
    }

    private void startListeningToMessages() {
        if (conversationId == null || conversationId.isEmpty()) return;

        messagesListener = ChatRepository.listenToMessages(conversationId,
                new ChatRepository.MessagesCallback() {
                    @Override
                    public void onSuccess(List<Message> messagesList) {
                        messages.clear();
                        messages.addAll(messagesList);
                        adapter.notifyDataSetChanged();

                        // Scroll to bottom
                        if (!messages.isEmpty()) {
                            binding.recyclerMessages.scrollToPosition(messages.size() - 1);
                        }

                        // Try to get other user's name from messages if not found yet
                        if ((otherUserName == null || otherUserName.equals("Item Owner") || otherUserName.equals("User"))
                                && otherUserId != null) {
                            for (Message msg : messagesList) {
                                if (otherUserId.equals(msg.getSenderId())) {
                                    String name = msg.getSenderName();
                                    if (name != null && !name.isEmpty() && !name.equals("Item Owner")) {
                                        otherUserName = name;
                                        updateHeaderUI();
                                        break;
                                    }
                                }
                            }
                        }

                        updateEmptyState();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error loading messages", e);
                    }
                });
    }

    private void stopListeningToMessages() {
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
    }

    private void updateEmptyState() {
        if (messages.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerMessages.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerMessages.setVisibility(View.VISIBLE);
        }
    }

    private void sendMessage() {
        String text = binding.editMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        // Clear input immediately
        binding.editMessage.setText("");

        ChatRepository.sendMessage(conversationId, currentUserId, currentUserName, text,
                new ChatRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        // Message will appear via listener
                        Log.d(TAG, "Message sent successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error sending message", e);
                        Toast.makeText(ChatActivity.this,
                                R.string.error_sending_message,
                                Toast.LENGTH_SHORT).show();
                        // Restore the message text
                        binding.editMessage.setText(text);
                    }
                });
    }
}