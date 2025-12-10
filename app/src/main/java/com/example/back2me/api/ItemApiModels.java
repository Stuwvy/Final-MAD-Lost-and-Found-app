package com.example.back2me.api;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

// ItemRequest class
class ItemRequest {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("location")
    private String location;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status; // "lost" or "found"

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("createdDate")
    private String createdDate; // ISO 8601 format

    // Default constructor
    public ItemRequest() {
        this.id = UUID.randomUUID().toString();
    }

    // Full constructor
    public ItemRequest(String id, String name, String location, String description,
                       String status, String createdBy, String createdDate) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}

// ItemResponse class
class ItemResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("location")
    private String location;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("createdDate")
    private String createdDate;

    // Default constructor
    public ItemResponse() {}

    // Full constructor
    public ItemResponse(String id, String name, String location, String description,
                        String status, String createdBy, String createdDate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}