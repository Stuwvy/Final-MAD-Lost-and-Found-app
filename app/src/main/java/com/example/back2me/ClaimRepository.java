package com.example.back2me;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimRepository {

    private static final String COLLECTION_NAME = "claims";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Callback interfaces
    public interface ClaimsCallback {
        void onSuccess(List<Claim> claims);
        void onError(Exception e);
    }

    public interface SingleClaimCallback {
        void onSuccess(Claim claim);
        void onError(Exception e);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Get claims by item (sorted locally to avoid index requirement)
    public static void getClaimsByItem(String itemId, ClaimsCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Claim> claims = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Claim claim = documentToClaim(document);
                        claims.add(claim);
                    }
                    // Sort locally by createdDate descending
                    sortClaimsByDateDesc(claims);
                    callback.onSuccess(claims);
                })
                .addOnFailureListener(callback::onError);
    }

    // Get claims by claimer (sorted locally)
    public static void getClaimsByClaimer(String claimerId, ClaimsCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("claimerId", claimerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Claim> claims = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Claim claim = documentToClaim(document);
                        claims.add(claim);
                    }
                    // Sort locally
                    sortClaimsByDateDesc(claims);
                    callback.onSuccess(claims);
                })
                .addOnFailureListener(callback::onError);
    }

    // Get claims by owner (sorted locally)
    public static void getClaimsByOwner(String ownerId, ClaimsCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Claim> claims = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Claim claim = documentToClaim(document);
                        claims.add(claim);
                    }
                    // Sort locally
                    sortClaimsByDateDesc(claims);
                    callback.onSuccess(claims);
                })
                .addOnFailureListener(callback::onError);
    }

    // Create claim
    public static void createClaim(Claim claim, OperationCallback callback) {
        Map<String, Object> claimData = claimToMap(claim);

        db.collection(COLLECTION_NAME)
                .add(claimData)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("id", documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onError);
    }

    // Update claim status
    public static void updateClaimStatus(String claimId, String status, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(claimId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Delete claim
    public static void deleteClaim(String claimId, OperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(claimId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Helper: Sort claims by date descending
    private static void sortClaimsByDateDesc(List<Claim> claims) {
        Collections.sort(claims, (c1, c2) -> {
            String date1 = c1.getCreatedDate();
            String date2 = c2.getCreatedDate();
            if (date1 == null) date1 = "";
            if (date2 == null) date2 = "";
            return date2.compareTo(date1); // Descending
        });
    }

    // Helper methods
    private static Claim documentToClaim(DocumentSnapshot document) {
        Claim claim = new Claim();
        claim.setId(document.getId());
        claim.setItemId(document.getString("itemId"));
        claim.setItemName(document.getString("itemName"));
        claim.setItemStatus(document.getString("itemStatus"));
        claim.setClaimerId(document.getString("claimerId"));
        claim.setClaimerName(document.getString("claimerName"));
        claim.setClaimerEmail(document.getString("claimerEmail"));
        claim.setOwnerId(document.getString("ownerId"));
        claim.setMessage(document.getString("message"));
        claim.setStatus(document.getString("status"));
        claim.setCreatedDate(document.getString("createdDate"));
        return claim;
    }

    private static Map<String, Object> claimToMap(Claim claim) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", claim.getId());
        map.put("itemId", claim.getItemId());
        map.put("itemName", claim.getItemName());
        map.put("itemStatus", claim.getItemStatus());
        map.put("claimerId", claim.getClaimerId());
        map.put("claimerName", claim.getClaimerName());
        map.put("claimerEmail", claim.getClaimerEmail());
        map.put("ownerId", claim.getOwnerId());
        map.put("message", claim.getMessage());
        map.put("status", claim.getStatus());
        map.put("createdDate", claim.getCreatedDate());
        return map;
    }
}