package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FaceTimeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        if (data["type"] == "INCOMING_CALL") {
            val callId = data[Constants.EXTRA_CALL_ID] ?: ""
            val callerName = data[Constants.EXTRA_CALLER_NAME] ?: "Incoming FaceTime"
            val callerPhoto = data[Constants.EXTRA_CALLER_PHOTO] ?: ""
            val isVideo = data[Constants.EXTRA_IS_VIDEO] == "true"

            showIncomingCallNotification(callId, callerName, callerPhoto, isVideo)
        }
    }

    private fun showIncomingCallNotification(
        callId: String,
        callerName: String,
        callerPhoto: String,
        isVideo: Boolean
    ) {
        val channelId = "facetime_calls_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Incoming FaceTime Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications for incoming video/audio calls"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            action = Constants.ACTION_INCOMING_CALL
            putExtra(Constants.EXTRA_CALL_ID, callId)
            putExtra(Constants.EXTRA_CALLER_NAME, callerName)
            putExtra(Constants.EXTRA_CALLER_PHOTO, callerPhoto)
            putExtra(Constants.EXTRA_IS_VIDEO, isVideo)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming FaceTime ${if (isVideo) "Video Call" else "Audio Call"}")
            .setContentText("$callerName is calling...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refreshed logic
    }
}
