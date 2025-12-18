package com.example.back2me;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRepository {

    private static final String COLLECTION_NAME = "items";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Callback interfaces
    public interface ItemsCallback {
        void onSuccess(List<Item> items);
        void onError(Exception e);
    }

    public interface SingleItemCallback {
        void onSuccess(Item item);
        void onError(Exception e);
    }

    public interface CreateCallback {
        void onSuccess(Item item);
        void onError(Exception e);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Get all items (sorted locally to avoid index requirement)
    public static void getAllItems(ItemsCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = documentToItem(document);
                        items.add(item);
                    }
                    // Sort locally by createdDate descending
                    sortItemsByDateDesc(items);
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    // Get items by status (sorted locally)
    public static void getItemsByStatus(String status, ItemsCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = documentToItem(document);
                        items.add(item);
                    }
                    // Sort locally
                    sortItemsByDateDesc(items);
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    // Get items by user (sorted locally to avoid index requirement)
    public static void getItemsByUser(String userId, ItemsCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = documentToItem(document);
                        items.add(item);
                    }
                    // Sort locally by createdDate descending
                    sortItemsByDateDesc(items);
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    // Get single item by ID
    public static void getItemById(String itemId, SingleItemCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(itemId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Item item = documentToItem(document);
                        callback.onSuccess(item);
                    } else {
                        callback.onError(new Exception("Item not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    // Search items (sorted locally)
    public static void searchItems(String query, ItemsCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> items = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = documentToItem(document);
                        if (item.getName().toLowerCase().contains(lowerQuery) ||
                                item.getLocation().toLowerCase().contains(lowerQuery) ||
                                item.getDescription().toLowerCase().contains(lowerQuery)) {
                            items.add(item);
                        }
                    }
                    // Sort locally
                    sortItemsByDateDesc(items);
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    // Create item
    public static void createItem(Item item, CreateCallback callback) {
        Map<String, Object> itemData = itemToMap(item);

        db.collection(COLLECTION_NAME)
                .add(itemData)
                .addOnSuccessListener(documentReference -> {
                    item.setId(documentReference.getId());
                    documentReference.update("id", documentReference.getId());
                    callback.onSuccess(item);
                })
                .addOnFailureListener(callback::onError);
    }

    // Update item
    public static void updateItem(Item item, UpdateCallback callback) {
        if (item.getId() == null || item.getId().isEmpty()) {
            callback.onError(new Exception("Item ID is required"));
            return;
        }

        Map<String, Object> itemData = itemToMap(item);

        db.collection(COLLECTION_NAME)
                .document(item.getId())
                .update(itemData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Delete item
    public static void deleteItem(String itemId, DeleteCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Helper: Sort items by date descending
    private static void sortItemsByDateDesc(List<Item> items) {
        Collections.sort(items, (item1, item2) -> {
            String date1 = item1.getCreatedDate();
            String date2 = item2.getCreatedDate();
            if (date1 == null) date1 = "";
            if (date2 == null) date2 = "";
            return date2.compareTo(date1); // Descending
        });
    }

    // Helper: Document to Item
    private static Item documentToItem(DocumentSnapshot document) {
        Item item = new Item();
        item.setId(document.getId());
        item.setName(document.getString("name"));
        item.setLocation(document.getString("location"));
        item.setDescription(document.getString("description"));
        item.setStatus(document.getString("status"));
        item.setCreatedBy(document.getString("createdBy"));
        item.setCreatedDate(document.getString("createdDate"));
        item.setImageUrl(document.getString("imageUrl"));
        return item;
    }

    // Helper: Item to Map
    private static Map<String, Object> itemToMap(Item item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("name", item.getName());
        map.put("location", item.getLocation());
        map.put("description", item.getDescription());
        map.put("status", item.getStatus());
        map.put("createdBy", item.getCreatedBy());
        map.put("createdDate", item.getCreatedDate());
        map.put("imageUrl", item.getImageUrl());
        return map;
    }
}