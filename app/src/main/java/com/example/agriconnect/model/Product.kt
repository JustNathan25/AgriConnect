package com.example.agriconnect.model

data class Product(
    var id: String = "",
    var name: String = "",
    var price: Double = 0.0,                  // change to Double
    var condition: String = "",
    var description: String = "",
    var images: List<String> = emptyList(),
    var locationName: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var sellerId: String? = null
)

