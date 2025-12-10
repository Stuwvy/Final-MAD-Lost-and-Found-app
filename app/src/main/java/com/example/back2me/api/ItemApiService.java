// file: ItemApiService.java
package com.example.back2me.api;

import retrofit2.Response;
import retrofit2.http.*;
import java.util.List;

public interface ItemApiService {

    @GET("items")
    Response<List<ItemResponse>> getItems();

    @POST("items")
    Response<ItemResponse> createItem(@Body ItemRequest item);

    @GET("items/{id}")
    Response<ItemResponse> getItemById(@Path("id") String id);

    @DELETE("items/{id}")
    Response<Void> deleteItem(@Path("id") String id);
}