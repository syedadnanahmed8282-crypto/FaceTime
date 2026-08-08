package com.example.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = STATUS_SENT, // "sent", "delivered"
    val replyToId: String? = null,
    val replyToText: String? = null,
    val reactions: Map<String, String> = emptyMap() // key: userId, value: emoji
) {
    companion object {
        const val STATUS_SENT = "sent"
        const val STATUS_DELIVERED = "delivered"
    }
}
