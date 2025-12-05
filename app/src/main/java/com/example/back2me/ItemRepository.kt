package com.example.back2me

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object ItemRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val itemsCollection = firestore.collection("items")

    // Get all items
    suspend fun getAllItems(): List<Item> {
        return try {
            val snapshot = itemsCollection
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Item::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error getting items", e)
            emptyList()
        }
    }

    // Create new item
    suspend fun createItem(item: Item): Result<Item> {
        return try {
            val docRef = itemsCollection.add(item).await()
            Result.success(item.copy(id = docRef.id))
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error creating item", e)
            Result.failure(e)
        }
    }

    // Get item by ID
    suspend fun getItemById(id: String): Item? {
        return try {
            val doc = itemsCollection.document(id).get().await()
            doc.toObject(Item::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error getting item", e)
            null
        }
    }

    // Update item
    suspend fun updateItem(id: String, item: Item): Result<Unit> {
        return try {
            itemsCollection.document(id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error updating item", e)
            Result.failure(e)
        }
    }

    // Delete item
    suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            itemsCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error deleting item", e)
            Result.failure(e)
        }
    }
}