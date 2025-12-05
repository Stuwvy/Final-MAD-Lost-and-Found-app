package com.example.back2me

data class Item(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val status: String = "lost", // "lost" or "found"
    val createdBy: String = "",
    val createdDate: String = "",
    val imageUrl: String = ""
)