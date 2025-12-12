package com.example.back2me;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ClaimRepository {

    private static final String TAG = "ClaimRepository";
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "claims";

    // Callback interfaces
    public interface ClaimsCallback {
        void onSuccess(List<Claim> claims);
        void onError(Exception e);
    }

    public interface ClaimCallback {
        void onSuccess(Claim claim);
        void onError(Exception e);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface CreateCallback {
        void onSuccess(Claim claim);
        void onError(Exception e);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
        void onError(Exception e);
    }

    // Create new claim
    public static void createClaim(Claim claim, CreateCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .add(claim)
                .addOnSuccessListener(documentReference -> {
                    Claim createdClaim = claim.copyWithId(documentReference.getId());
                    callback.onSuccess(createdClaim);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating claim", e);
                    callback.onError(e);
                });
    }

    // Get claims by item ID (for item owner to see who claimed)
    public static void getClaimsByItem(String itemId, ClaimsCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("itemId", itemId)
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Claim> claims = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Claim claim = doc.toObject(Claim.class);
                        if (claim != null) {
                            claim.setId(doc.getId());
                            claims.add(claim);
                        }
                    }
                    callback.onSuccess(claims);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting claims by item", e);
                    callback.onError(e);
                });
    }

    // Get claims by owner ID (for owner to see all claims on their items)
    public static void getClaimsByOwner(String ownerId, ClaimsCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("ownerId", ownerId)
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Claim> claims = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Claim claim = doc.toObject(Claim.class);
                        if (claim != null) {
                            claim.setId(doc.getId());
                            claims.add(claim);
                        }
                    }
                    callback.onSuccess(claims);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting claims by owner", e);
                    callback.onError(e);
                });
    }

    // Get claims made by a user (claimer)
    public static void getClaimsByClaimer(String claimerId, ClaimsCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("claimerId", claimerId)
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Claim> claims = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Claim claim = doc.toObject(Claim.class);
                        if (claim != null) {
                            claim.setId(doc.getId());
                            claims.add(claim);
                        }
                    }
                    callback.onSuccess(claims);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting claims by claimer", e);
                    callback.onError(e);
                });
    }

    // Check if user already claimed an item
    public static void hasUserClaimedItem(String itemId, String claimerId, ExistsCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("claimerId", claimerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onResult(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking claim", e);
                    callback.onError(e);
                });
    }

    // Update claim status (approve/reject)
    public static void updateClaimStatus(String claimId, String newStatus, OperationCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .document(claimId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating claim status", e);
                    callback.onError(e);
                });
    }

    // Delete claim
    public static void deleteClaim(String claimId, OperationCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .document(claimId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting claim", e);
                    callback.onError(e);
                });
    }

    // Get pending claims count for owner
    public static void getPendingClaimsCount(String ownerId, CountCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onSuccess(querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting pending claims count", e);
                    callback.onError(e);
                });
    }

    public interface CountCallback {
        void onSuccess(int count);
        void onError(Exception e);
    }
}
