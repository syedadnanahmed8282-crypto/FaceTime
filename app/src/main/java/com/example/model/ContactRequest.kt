package com.example.model

data class ContactRequest(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val senderPhoto: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending"
)
