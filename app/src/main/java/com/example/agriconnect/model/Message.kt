package com.example.agriconnect.model

import com.google.firebase.Timestamp

data class Message(
    var senderId: String = "",
    var text: String = "",
    var timestamp: Timestamp? = null
)
