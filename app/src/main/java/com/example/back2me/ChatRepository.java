package com.example.back2me;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private static final String TAG = "ChatRepository";
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final String CONVERSATIONS_COLLECTION = "conversations";
    private static final String MESSAGES_COLLECTION = "messages";

    // Callback interfaces
    public interface ConversationsCallback {
        void onSuccess(List<Conversation> conversations);
        void onError(Exception e);
    }

    public interface ConversationCallback {
        void onSuccess(Conversation conversation);
        void onError(Exception e);
    }

    public interface MessagesCallback {
        void onSuccess(List<Message> messages);
        void onError(Exception e);
    }

    public interface MessageCallback {
        void onSuccess(Message message);
        void onError(Exception e);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface StringCallback {
        void onSuccess(String result);
        void onError(Exception e);
    }

    // ==================== CONVERSATIONS ====================

    // Get all conversations for a user
    public static void getConversations(String userId, ConversationsCallback callback) {
        firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participantIds", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Conversation> conversations = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Conversation conversation = doc.toObject(Conversation.class);
                        if (conversation != null) {
                            conversation.setId(doc.getId());
                            conversations.add(conversation);
                        }
                    }
                    callback.onSuccess(conversations);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting conversations", e);
                    callback.onError(e);
                });
    }

    // Listen to conversations in real-time
    public static ListenerRegistration listenToConversations(String userId, ConversationsCallback callback) {
        return firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participantIds", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to conversations", e);
                        callback.onError(e);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Conversation> conversations = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Conversation conversation = doc.toObject(Conversation.class);
                            if (conversation != null) {
                                conversation.setId(doc.getId());
                                conversations.add(conversation);
                            }
                        }
                        callback.onSuccess(conversations);
                    }
                });
    }

    // Find or create conversation between two users
    public static void findOrCreateConversation(String user1Id, String user1Name,
                                                 String user2Id, String user2Name,
                                                 String itemId, String itemName,
                                                 StringCallback callback) {
        // First, try to find existing conversation
        firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participantIds", user1Id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    String existingConversationId = null;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participantIds");
                        if (participants != null && participants.contains(user2Id)) {
                            existingConversationId = doc.getId();
                            break;
                        }
                    }

                    if (existingConversationId != null) {
                        callback.onSuccess(existingConversationId);
                    } else {
                        // Create new conversation
                        createConversation(user1Id, user1Name, user2Id, user2Name, 
                                          itemId, itemName, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding conversation", e);
                    callback.onError(e);
                });
    }

    // Create a new conversation
    private static void createConversation(String user1Id, String user1Name,
                                           String user2Id, String user2Name,
                                           String itemId, String itemName,
                                           StringCallback callback) {
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("participantIds", Arrays.asList(user1Id, user2Id));
        
        Map<String, String> participantNames = new HashMap<>();
        participantNames.put(user1Id, user1Name);
        participantNames.put(user2Id, user2Name);
        conversationData.put("participantNames", participantNames);
        
        conversationData.put("lastMessage", "");
        conversationData.put("lastMessageTime", "");
        conversationData.put("lastSenderId", "");
        conversationData.put("itemId", itemId != null ? itemId : "");
        conversationData.put("itemName", itemName != null ? itemName : "");
        conversationData.put("unreadCount", 0);

        firestore.collection(CONVERSATIONS_COLLECTION)
                .add(conversationData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating conversation", e);
                    callback.onError(e);
                });
    }

    // Get conversation by ID
    public static void getConversationById(String conversationId, ConversationCallback callback) {
        firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Conversation conversation = doc.toObject(Conversation.class);
                        if (conversation != null) {
                            conversation.setId(doc.getId());
                            callback.onSuccess(conversation);
                        } else {
                            callback.onError(new Exception("Failed to parse conversation"));
                        }
                    } else {
                        callback.onError(new Exception("Conversation not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting conversation", e);
                    callback.onError(e);
                });
    }

    // ==================== MESSAGES ====================

    // Send a message
    public static void sendMessage(String conversationId, Message message, MessageCallback callback) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", message.getSenderId());
        messageData.put("senderName", message.getSenderName());
        messageData.put("text", message.getText());
        messageData.put("timestamp", message.getTimestamp());
        messageData.put("read", false);

        firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    // Update conversation's last message
                    updateConversationLastMessage(conversationId, message);
                    
                    message.setId(documentReference.getId());
                    callback.onSuccess(message);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    callback.onError(e);
                });
    }

    // Update conversation's last message info
    private static void updateConversationLastMessage(String conversationId, Message message) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", message.getText());
        updates.put("lastMessageTime", message.getTimestamp());
        updates.put("lastSenderId", message.getSenderId());

        firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating last message", e));
    }

    // Get messages for a conversation
    public static void getMessages(String conversationId, MessagesCallback callback) {
        firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Message message = doc.toObject(Message.class);
                        if (message != null) {
                            message.setId(doc.getId());
                            messages.add(message);
                        }
                    }
                    callback.onSuccess(messages);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting messages", e);
                    callback.onError(e);
                });
    }

    // Listen to messages in real-time
    public static ListenerRegistration listenToMessages(String conversationId, MessagesCallback callback) {
        return firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to messages", e);
                        callback.onError(e);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Message> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                message.setId(doc.getId());
                                messages.add(message);
                            }
                        }
                        callback.onSuccess(messages);
                    }
                });
    }

    // Mark messages as read
    public static void markMessagesAsRead(String conversationId, String currentUserId) {
        firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .whereNotEqualTo("senderId", currentUserId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().update("read", true);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error marking messages as read", e));
    }
}
