package com.example.back2me;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ItemRepository {

    private static final String TAG = "ItemRepository";
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "items";

    // Callback interfaces
    public interface ItemsCallback {
        void onSuccess(List<Item> items);
        void onError(Exception e);
    }

    public interface ItemCallback {
        void onSuccess(Item item);
        void onError(Exception e);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface CreateCallback {
        void onSuccess(Item item);
        void onError(Exception e);
    }

    // Get all items
    public static void getAllItems(ItemsCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Item> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting items", e);
                    callback.onError(e);
                });
    }

    // Create new item
    public static void createItem(Item item, CreateCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .add(item)
                .addOnSuccessListener(documentReference -> {
                    Item createdItem = item.copyWithId(documentReference.getId());
                    callback.onSuccess(createdItem);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating item", e);
                    callback.onError(e);
                });
    }

    // Get item by ID
    public static void getItemById(String id, ItemCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            item.setId(doc.getId());
                        }
                        callback.onSuccess(item);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting item", e);
                    callback.onError(e);
                });
    }

    // Update item
    public static void updateItem(String id, Item item, OperationCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .set(item)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating item", e);
                    callback.onError(e);
                });
    }

    // Delete item
    public static void deleteItem(String id, OperationCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting item", e);
                    callback.onError(e);
                });
    }

    // Get items by user ID
    public static void getItemsByUser(String userId, ItemsCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("createdBy", userId)
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Item> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user items", e);
                    callback.onError(e);
                });
    }

    // Get items by status (lost or found)
    public static void getItemsByStatus(String status, ItemsCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Item> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting items by status", e);
                    callback.onError(e);
                });
    }
}