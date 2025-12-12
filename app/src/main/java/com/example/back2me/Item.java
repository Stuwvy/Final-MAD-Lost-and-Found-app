package com.example.back2me;

public class Item {
    private String id;
    private String name;
    private String location;
    private String description;
    private String status; // "lost" or "found"
    private String createdBy;
    private String createdDate;
    private String imageUrl;

    // Empty constructor required for Firestore
    public Item() {
        this.id = "";
        this.name = "";
        this.location = "";
        this.description = "";
        this.status = "lost";
        this.createdBy = "";
        this.createdDate = "";
        this.imageUrl = "";
    }

    // Full constructor
    public Item(String id, String name, String location, String description,
                String status, String createdBy, String createdDate, String imageUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.imageUrl = imageUrl;
    }

    // Constructor without ID (for creating new items)
    public Item(String name, String location, String description,
                String status, String createdBy, String createdDate, String imageUrl) {
        this.id = "";
        this.name = name;
        this.location = location;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedDate() { return createdDate; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Copy with new ID
    public Item copyWithId(String newId) {
        return new Item(newId, name, location, description, status, createdBy, createdDate, imageUrl);
    }
}