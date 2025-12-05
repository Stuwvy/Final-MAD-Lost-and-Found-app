// file: ItemApiService.kt (CONFIRMED CORRECT)
package com.example.back2me.api

import retrofit2.Response
import retrofit2.http.*

interface ItemApiService {

    @GET("items")
    suspend fun getItems(): Response<List<ItemResponse>> // CORRECT: No header argument

    @POST("items")
    suspend fun createItem(
        @Body item: ItemRequest
    ): Response<ItemResponse> // CORRECT: No header argument

    @GET("items/{id}")
    suspend fun getItemById(
        @Path("id") id: String
    ): Response<ItemResponse> // CORRECT: No header argument

    @DELETE("items/{id}")
    suspend fun deleteItem(
        @Path("id") id: String
    ): Response<Void> // CORRECT: No header argument
}