package com.example.back2me.api

import com.google.gson.annotations.SerializedName
import java.util.*

data class ItemRequest(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("name")
    val name: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("status")
    val status: String, // "lost" or "found"

    @SerializedName("createdBy")
    val createdBy: String,

    @SerializedName("createdDate")
    val createdDate: String // ISO 8601 format
)

data class ItemResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdBy")
    val createdBy: String,

    @SerializedName("createdDate")
    val createdDate: String
)