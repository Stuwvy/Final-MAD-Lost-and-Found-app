package com.example.back2me;

public class Claim {
    private String id;
    private String itemId;
    private String itemName;
    private String claimerId;          // User who is claiming
    private String claimerEmail;
    private String claimerName;
    private String ownerId;            // Item owner
    private String message;            // Claim message/description
    private String status;             // "pending", "approved", "rejected"
    private String createdDate;
    private String itemStatus;         // "lost" or "found" - to show appropriate text

    // Empty constructor required for Firestore
    public Claim() {
        this.id = "";
        this.itemId = "";
        this.itemName = "";
        this.claimerId = "";
        this.claimerEmail = "";
        this.claimerName = "";
        this.ownerId = "";
        this.message = "";
        this.status = "pending";
        this.createdDate = "";
        this.itemStatus = "";
    }

    // Full constructor
    public Claim(String id, String itemId, String itemName, String claimerId, 
                 String claimerEmail, String claimerName, String ownerId,
                 String message, String status, String createdDate, String itemStatus) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.claimerId = claimerId;
        this.claimerEmail = claimerEmail;
        this.claimerName = claimerName;
        this.ownerId = ownerId;
        this.message = message;
        this.status = status;
        this.createdDate = createdDate;
        this.itemStatus = itemStatus;
    }

    // Constructor without ID (for creating new claims)
    public Claim(String itemId, String itemName, String claimerId, String claimerEmail,
                 String claimerName, String ownerId, String message, String createdDate, String itemStatus) {
        this.id = "";
        this.itemId = itemId;
        this.itemName = itemName;
        this.claimerId = claimerId;
        this.claimerEmail = claimerEmail;
        this.claimerName = claimerName;
        this.ownerId = ownerId;
        this.message = message;
        this.status = "pending";
        this.createdDate = createdDate;
        this.itemStatus = itemStatus;
    }

    // Getters
    public String getId() { return id; }
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getClaimerId() { return claimerId; }
    public String getClaimerEmail() { return claimerEmail; }
    public String getClaimerName() { return claimerName; }
    public String getOwnerId() { return ownerId; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getCreatedDate() { return createdDate; }
    public String getItemStatus() { return itemStatus; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setClaimerId(String claimerId) { this.claimerId = claimerId; }
    public void setClaimerEmail(String claimerEmail) { this.claimerEmail = claimerEmail; }
    public void setClaimerName(String claimerName) { this.claimerName = claimerName; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }

    // Copy with new ID
    public Claim copyWithId(String newId) {
        return new Claim(newId, itemId, itemName, claimerId, claimerEmail, 
                         claimerName, ownerId, message, status, createdDate, itemStatus);
    }
}
