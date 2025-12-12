package com.example.back2me;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String text;
    private String timestamp;
    private boolean read;

    // Empty constructor for Firestore
    public Message() {
        this.id = "";
        this.senderId = "";
        this.senderName = "";
        this.text = "";
        this.timestamp = "";
        this.read = false;
    }

    // Full constructor
    public Message(String id, String senderId, String senderName, String text, 
                   String timestamp, boolean read) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Constructor without ID
    public Message(String senderId, String senderName, String text, String timestamp) {
        this.id = "";
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.read = false;
    }

    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public String getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { this.read = read; }
}
