package com.example.ui.screens

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.agora.AgoraManager
import com.example.model.CallSession
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassmorphismBorder
import com.example.ui.theme.PlatinumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlueCard
import com.example.ui.theme.RoyalBlueDark
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    callSession: CallSession,
    otherUserName: String,
    otherUserPhoto: String,
    agoraManager: AgoraManager,
    isMuted: Boolean,
    isVideoDisabled: Boolean,
    isSpeakerOn: Boolean,
    remoteUid: Int?,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    var callDurationSeconds by remember { mutableIntStateOf(0) }

    // Live call duration timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            agoraManager.leaveCall()
        }
    }

    val isAudioCallOnly = !callSession.isVideoCall

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RoyalBlueDark)
    ) {
        if (!isAudioCallOnly && remoteUid != null && !isVideoDisabled) {
            // Video Call Remote Surface View
            AndroidView(
                factory = { context ->
                    SurfaceView(context).apply {
                        setZOrderMediaOverlay(false)
                        agoraManager.setupRemoteVideo(this, remoteUid)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Audio Call Mode (or Video disabled/waiting) - Zero SurfaceView for Audio Mode
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                UserAvatar(
                    photoUrl = otherUserPhoto,
                    displayName = otherUserName,
                    size = 110.dp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = otherUserName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                val mins = callDurationSeconds / 60
                val secs = callDurationSeconds % 60
                val timeString = String.format("%02d:%02d", mins, secs)

                Text(
                    text = if (remoteUid != null) "FaceTime ${if (isAudioCallOnly) "Voice" else "Video"} Call ($timeString)" else "Ringing...",
                    fontSize = 15.sp,
                    color = ElectricCyan,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Local Stream PIP View (Video mode only)
        if (!isAudioCallOnly && !isVideoDisabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .size(width = 110.dp, height = 160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RoyalBlueCard)
                    .border(2.dp, GlassmorphismBorder, RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { context ->
                        SurfaceView(context).apply {
                            setZOrderMediaOverlay(true)
                            agoraManager.setupLocalVideo(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // In-Call Controls Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(RoyalBlueCard.copy(alpha = 0.9f))
                .border(1.dp, GlassmorphismBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute / Unmute
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isMuted) CallRed else Color.White.copy(alpha = 0.15f))
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute Mic",
                        tint = PureWhite
                    )
                }

                // Video controls (only active if video call mode)
                if (!isAudioCallOnly) {
                    IconButton(
                        onClick = onToggleVideo,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isVideoDisabled) CallRed else Color.White.copy(alpha = 0.15f))
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Disable Camera",
                            tint = PureWhite
                        )
                    }

                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchCamera,
                            contentDescription = "Switch Camera",
                            tint = PureWhite
                        )
                    }
                }

                // Speakerphone Toggle
                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSpeakerOn) ElectricCyan else Color.White.copy(alpha = 0.15f))
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Toggle Speaker",
                        tint = if (isSpeakerOn) RoyalBlueDark else PureWhite
                    )
                }

                // End Call Red Button
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = CallRed,
                    contentColor = PureWhite,
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call"
                    )
                }
            }
        }
    }
}
