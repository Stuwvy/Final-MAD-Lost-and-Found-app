package com.example.back2me;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChatRepository {

    private static final String CONVERSATIONS_COLLECTION = "conversations";
    private static final String MESSAGES_COLLECTION = "messages";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Get conversations for user (without orderBy to avoid index requirement)
    public static void getConversationsForUser(String userId, ConversationsCallback callback) {
        db.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participants", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Conversation> conversations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Conversation conversation = documentToConversation(document);
                        conversations.add(conversation);
                    }
                    // Sort locally by lastMessageTime descending
                    sortConversationsByDateDesc(conversations);
                    callback.onSuccess(conversations);
                })
                .addOnFailureListener(callback::onError);
    }

    // Get or create conversation
    public static void getOrCreateConversation(
            String user1Id, String user1Name,
            String user2Id, String user2Name,
            String itemId, String itemName,
            ConversationCallback callback) {

        // Check for existing conversation
        db.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participants", user1Id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Conversation existingConversation = null;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> participants = (List<String>) document.get("participants");
                        String docItemId = document.getString("itemId");

                        if (participants != null && participants.contains(user2Id) &&
                                itemId.equals(docItemId)) {
                            existingConversation = documentToConversation(document);
                            break;
                        }
                    }

                    if (existingConversation != null) {
                        callback.onSuccess(existingConversation);
                    } else {
                        // Create new conversation
                        createConversation(user1Id, user1Name, user2Id, user2Name, itemId, itemName, callback);
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private static void createConversation(
            String user1Id, String user1Name,
            String user2Id, String user2Name,
            String itemId, String itemName,
            ConversationCallback callback) {

        String timestamp = getCurrentTimestamp();

        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("participants", Arrays.asList(user1Id, user2Id));
        conversationData.put("participantNames", createParticipantNamesMap(user1Id, user1Name, user2Id, user2Name));
        conversationData.put("itemId", itemId);
        conversationData.put("itemName", itemName);
        conversationData.put("lastMessage", "");
        conversationData.put("lastMessageTime", timestamp);
        conversationData.put("createdAt", timestamp);

        db.collection(CONVERSATIONS_COLLECTION)
                .add(conversationData)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("id", documentReference.getId());

                    Conversation conversation = new Conversation();
                    conversation.setId(documentReference.getId());
                    conversation.setParticipants(Arrays.asList(user1Id, user2Id));
                    conversation.setItemId(itemId);
                    conversation.setItemName(itemName);
                    conversation.setLastMessage("");
                    conversation.setLastMessageTime(timestamp);

                    callback.onSuccess(conversation);
                })
                .addOnFailureListener(callback::onError);
    }

    // Send message
    public static void sendMessage(String conversationId, String senderId, String senderName, String text, OperationCallback callback) {
        String timestamp = getCurrentTimestamp();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("senderName", senderName);
        messageData.put("text", text);
        messageData.put("timestamp", timestamp);

        db.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    // Update last message in conversation
                    db.collection(CONVERSATIONS_COLLECTION)
                            .document(conversationId)
                            .update("lastMessage", text, "lastMessageTime", timestamp)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    // Listen to messages (uses orderBy on single field - no index needed)
    public static ListenerRegistration listenToMessages(String conversationId, MessagesCallback callback) {
        return db.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onError(e);
                        return;
                    }

                    List<Message> messages = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Message message = documentToMessage(document);
                            messages.add(message);
                        }
                    }
                    callback.onSuccess(messages);
                });
    }

    // Helper: Sort conversations by date descending
    private static void sortConversationsByDateDesc(List<Conversation> conversations) {
        Collections.sort(conversations, (c1, c2) -> {
            String date1 = c1.getLastMessageTime();
            String date2 = c2.getLastMessageTime();
            if (date1 == null) date1 = "";
            if (date2 == null) date2 = "";
            return date2.compareTo(date1); // Descending
        });
    }

    private static Map<String, String> createParticipantNamesMap(String user1Id, String user1Name, String user2Id, String user2Name) {
        Map<String, String> names = new HashMap<>();
        names.put(user1Id, user1Name);
        names.put(user2Id, user2Name);
        return names;
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormat.format(new Date());
    }

    private static Conversation documentToConversation(DocumentSnapshot document) {
        Conversation conversation = new Conversation();
        conversation.setId(document.getId());
        conversation.setParticipants((List<String>) document.get("participants"));
        conversation.setParticipantNames((Map<String, String>) document.get("participantNames"));
        conversation.setItemId(document.getString("itemId"));
        conversation.setItemName(document.getString("itemName"));
        conversation.setLastMessage(document.getString("lastMessage"));
        conversation.setLastMessageTime(document.getString("lastMessageTime"));
        return conversation;
    }

    private static Message documentToMessage(DocumentSnapshot document) {
        Message message = new Message();
        message.setId(document.getId());
        message.setSenderId(document.getString("senderId"));
        message.setSenderName(document.getString("senderName"));
        message.setText(document.getString("text"));
        message.setTimestamp(document.getString("timestamp"));
        return message;
    }
}