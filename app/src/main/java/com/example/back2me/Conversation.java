package com.example.back2me;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conversation {
    private String id;
    private List<String> participantIds;
    private Map<String, String> participantNames;  // {userId: name}
    private String lastMessage;
    private String lastMessageTime;
    private String lastSenderId;
    private String itemId;          // Optional: related item
    private String itemName;        // Optional: related item name
    private int unreadCount;

    // Empty constructor for Firestore
    public Conversation() {
        this.id = "";
        this.participantIds = new ArrayList<>();
        this.participantNames = new HashMap<>();
        this.lastMessage = "";
        this.lastMessageTime = "";
        this.lastSenderId = "";
        this.itemId = "";
        this.itemName = "";
        this.unreadCount = 0;
    }

    // Full constructor
    public Conversation(String id, List<String> participantIds, Map<String, String> participantNames,
                        String lastMessage, String lastMessageTime, String lastSenderId,
                        String itemId, String itemName, int unreadCount) {
        this.id = id;
        this.participantIds = participantIds != null ? participantIds : new ArrayList<>();
        this.participantNames = participantNames != null ? participantNames : new HashMap<>();
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastSenderId = lastSenderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.unreadCount = unreadCount;
    }

    // Get the other participant's name (for display)
    public String getOtherParticipantName(String currentUserId) {
        for (Map.Entry<String, String> entry : participantNames.entrySet()) {
            if (!entry.getKey().equals(currentUserId)) {
                return entry.getValue();
            }
        }
        return "Unknown";
    }

    // Get the other participant's ID
    public String getOtherParticipantId(String currentUserId) {
        for (String participantId : participantIds) {
            if (!participantId.equals(currentUserId)) {
                return participantId;
            }
        }
        return "";
    }

    // Getters
    public String getId() { return id; }
    public List<String> getParticipantIds() { return participantIds; }
    public Map<String, String> getParticipantNames() { return participantNames; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public String getLastSenderId() { return lastSenderId; }
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getUnreadCount() { return unreadCount; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public void setParticipantNames(Map<String, String> participantNames) { this.participantNames = participantNames; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
