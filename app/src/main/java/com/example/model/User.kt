package com.example.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val aboutStatus: String = "Available for FaceTime",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)
