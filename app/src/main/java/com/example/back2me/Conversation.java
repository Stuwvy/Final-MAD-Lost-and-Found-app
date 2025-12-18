package com.example.back2me;

import java.util.List;
import java.util.Map;

public class Conversation {
    private String id;
    private List<String> participants;
    private Map<String, String> participantNames;
    private String itemId;
    private String itemName;
    private String lastMessage;
    private String lastMessageTime;

    public Conversation() {
    }

    public Conversation(String id, List<String> participants, Map<String, String> participantNames,
                        String itemId, String itemName, String lastMessage, String lastMessageTime) {
        this.id = id;
        this.participants = participants;
        this.participantNames = participantNames;
        this.itemId = itemId;
        this.itemName = itemName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    // Getters
    public String getId() {
        return id;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public Map<String, String> getParticipantNames() {
        return participantNames;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void setParticipantNames(Map<String, String> participantNames) {
        this.participantNames = participantNames;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    // Helper method to get other user's name
    public String getOtherUserName(String currentUserId) {
        if (participantNames == null) return "User";

        for (Map.Entry<String, String> entry : participantNames.entrySet()) {
            if (!entry.getKey().equals(currentUserId)) {
                return entry.getValue();
            }
        }
        return "User";
    }

    // Helper method to get other user's ID
    public String getOtherUserId(String currentUserId) {
        if (participants == null) return null;

        for (String participantId : participants) {
            if (!participantId.equals(currentUserId)) {
                return participantId;
            }
        }
        return null;
    }
}