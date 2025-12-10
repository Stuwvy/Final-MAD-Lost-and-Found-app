package com.example.back2me;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemRepository {
    private static final String TAG = "ItemRepository";
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "items";

    // Private constructor to prevent instantiation
    private ItemRepository() {}

    // Get all items
    public static CompletableFuture<List<Item>> getAllItems() {
        CompletableFuture<List<Item>> future = new CompletableFuture<>();

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
                    future.complete(items);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting items", e);
                    future.complete(new ArrayList<>());
                });

        return future;
    }

    // Create new item
    public static CompletableFuture<Result<Item>> createItem(Item item) {
        CompletableFuture<Result<Item>> future = new CompletableFuture<>();

        firestore.collection(COLLECTION_NAME)
                .add(item)
                .addOnSuccessListener(docRef -> {
                    Item newItem = item.copyWithId(docRef.getId());
                    future.complete(Result.success(newItem));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating item", e);
                    future.complete(Result.failure(e));
                });

        return future;
    }

    // Get item by ID
    public static CompletableFuture<Item> getItemById(String id) {
        CompletableFuture<Item> future = new CompletableFuture<>();

        firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            future.complete(item);
                        } else {
                            future.complete(null);
                        }
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting item", e);
                    future.complete(null);
                });

        return future;
    }

    // Update item
    public static CompletableFuture<Result<Void>> updateItem(String id, Item item) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();

        firestore.collection(COLLECTION_NAME)
                .document(id)
                .set(item)
                .addOnSuccessListener(aVoid -> future.complete(Result.success(null)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating item", e);
                    future.complete(Result.failure(e));
                });

        return future;
    }

    // Delete item
    public static CompletableFuture<Result<Void>> deleteItem(String id) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();

        firestore.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> future.complete(Result.success(null)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting item", e);
                    future.complete(Result.failure(e));
                });

        return future;
    }

    // Result class to handle success/failure
    public static class Result<T> {
        private final T data;
        private final Exception exception;
        private final boolean isSuccess;

        private Result(T data, Exception exception, boolean isSuccess) {
            this.data = data;
            this.exception = exception;
            this.isSuccess = isSuccess;
        }

        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }

        public static <T> Result<T> failure(Exception exception) {
            return new Result<>(null, exception, false);
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public T getData() {
            return data;
        }

        public Exception getException() {
            return exception;
        }
    }
}