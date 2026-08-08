package com.example.model

data class CallSession(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerPhoto: String = "",
    val receiverId: String = "",
    val channelId: String = "",
    val isVideoCall: Boolean = true,
    val status: String = STATUS_RINGING,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_RINGING = "ringing"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_DECLINED = "declined"
        const val STATUS_ENDED = "ended"
    }
}
