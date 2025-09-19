package com.example.agriconnect.model

data class Product(
    val name: String,
    val price: String,
    val location: String,
    val imageRes: Int // ✅ this must exist
)
