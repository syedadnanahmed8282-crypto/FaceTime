package com.example.utils

object Constants {
    // Web Client ID for Google Sign-In from google-services.json
    const val WEB_CLIENT_ID = "58321223762-5k3m41pk23hm52doo8fnuktnt4dgurg5.apps.googleusercontent.com"

    // Agora SDK Configuration
    const val AGORA_APP_ID = "80b874784d164d9aba38cea4626ba400"
    const val AGORA_APP_CERTIFICATE = "29019722a0b34972a08988eea1d69c2b"
    const val TOKEN_BASE_URL = ""

    // Firebase Database Nodes
    const val NODE_USERS = "users"
    const val NODE_CHATS = "chats"
    const val NODE_CALLS = "calls"
    const val NODE_CONTACTS = "contacts"
    const val NODE_CONTACT_REQUESTS = "contact_requests"

    // Call Actions for Broadcast / Intent
    const val ACTION_INCOMING_CALL = "com.example.facetime.INCOMING_CALL"
    const val EXTRA_CALL_ID = "extra_call_id"
    const val EXTRA_CALLER_ID = "extra_caller_id"
    const val EXTRA_CALLER_NAME = "extra_caller_name"
    const val EXTRA_CALLER_PHOTO = "extra_caller_photo"
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_IS_VIDEO = "extra_is_video"
}
