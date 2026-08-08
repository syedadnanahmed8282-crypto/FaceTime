package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PlatinumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlueCard
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueMedium

@Composable
fun UserProfileScreen(
    user: User,
    isMutualContact: Boolean,
    onOpenChat: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onAddContact: () -> Unit,
    onRemoveContact: () -> Unit,
    onDeleteChatHistory: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = RoyalBlueDark,
 topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalBlueMedium)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contact Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(
                photoUrl = user.photoUrl,
                displayName = user.displayName,
                isOnline = user.isOnline,
                size = 100.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.displayName,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite
            )

            Text(
                text = user.email.ifBlank { user.uid },
                fontSize = 13.sp,
                color = PlatinumGray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Privacy Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isMutualContact) CallGreen.copy(alpha = 0.2f) else CallRed.copy(alpha = 0.2f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isMutualContact) "Mutual Contact ✅ (Messaging & Calling Enabled)" else "Not Mutual Contact 🔒 (Messaging & Calling Restricted)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMutualContact) CallGreen else CallRed,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Info Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = RoyalBlueCard
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ABOUT / STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = user.aboutStatus,
                        fontSize = 15.sp,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ONLINE STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (user.isOnline) "Active Now 🟢" else "Offline 🔴",
                        fontSize = 14.sp,
                        color = if (user.isOnline) CallGreen else PlatinumGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons row (if mutual contact)
            if (isMutualContact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = onOpenChat,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = RoyalBlueDark
                        )
                    }

                    IconButton(
                        onClick = onStartVoiceCall,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CallGreen)
                            .size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = PureWhite
                        )
                    }

                    IconButton(
                        onClick = onStartVideoCall,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoCall,
                            contentDescription = "Video Call",
                            tint = RoyalBlueDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Contact Relationship Action Button
            if (isMutualContact) {
                OutlinedButton(
                    onClick = onRemoveContact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CallRed)
                ) {
                    Icon(imageVector = Icons.Default.PersonRemove, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Remove Contact", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onAddContact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = RoyalBlueDark
                    )
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add Contact / Accept Request", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Delete Chat History Action Button
            OutlinedButton(
                onClick = onDeleteChatHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CallRed)
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Delete Chat History", fontWeight = FontWeight.Bold)
            }
        }
    }
}
