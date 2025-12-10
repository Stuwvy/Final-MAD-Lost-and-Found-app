package com.example.back2me;

import java.util.Objects;

public class Item {
    private String id;
    private String name;
    private String location;
    private String description;
    private String status; // "lost" or "found"
    private String createdBy;
    private String createdDate;
    private String imageUrl;

    // Default constructor required for Firestore
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

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Copy method (like Kotlin's data class copy)
    public Item copy(String id, String name, String location, String description,
                     String status, String createdBy, String createdDate, String imageUrl) {
        return new Item(
                id != null ? id : this.id,
                name != null ? name : this.name,
                location != null ? location : this.location,
                description != null ? description : this.description,
                status != null ? status : this.status,
                createdBy != null ? createdBy : this.createdBy,
                createdDate != null ? createdDate : this.createdDate,
                imageUrl != null ? imageUrl : this.imageUrl
        );
    }

    // Convenience copy method for just ID
    public Item copyWithId(String id) {
        return new Item(id, this.name, this.location, this.description,
                this.status, this.createdBy, this.createdDate, this.imageUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id) &&
                Objects.equals(name, item.name) &&
                Objects.equals(location, item.location) &&
                Objects.equals(description, item.description) &&
                Objects.equals(status, item.status) &&
                Objects.equals(createdBy, item.createdBy) &&
                Objects.equals(createdDate, item.createdDate) &&
                Objects.equals(imageUrl, item.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location, description, status,
                createdBy, createdDate, imageUrl);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}