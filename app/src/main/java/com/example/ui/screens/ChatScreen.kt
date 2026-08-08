package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.User
import com.example.ui.components.MessageBubble
import com.example.ui.components.ReactionPicker
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassmorphismBorder
import com.example.ui.theme.PlatinumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlueCard
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserId: String,
    otherUser: User,
    isMutualContact: Boolean,
    messages: List<ChatMessage>,
    replyToMessage: ChatMessage?,
    onSendMessage: (String) -> Unit,
    onSetReplyTo: (ChatMessage?) -> Unit,
    onToggleReaction: (String, String) -> Unit,
    onStartVoiceCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onOpenProfile: () -> Unit,
    onDeleteChatHistory: () -> Unit,
    onBack: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var reactionTargetMsg by remember { mutableStateOf<ChatMessage?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = RoyalBlueDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalBlueMedium)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }

                UserAvatar(
                    photoUrl = otherUser.photoUrl,
                    displayName = otherUser.displayName,
                    isOnline = otherUser.isOnline,
                    size = 40.dp,
                    modifier = Modifier.clickable { onOpenProfile() }
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenProfile() }
                ) {
                    Text(
                        text = otherUser.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = if (isMutualContact) (if (otherUser.isOnline) "Active Now" else "Offline") else "Contact Not Added",
                        fontSize = 11.sp,
                        color = if (isMutualContact && otherUser.isOnline) CallGreen else PlatinumGray
                    )
                }

                if (isMutualContact) {
                    IconButton(onClick = onStartVoiceCall) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = CallGreen
                        )
                    }

                    IconButton(onClick = onStartVideoCall) {
                        Icon(
                            imageVector = Icons.Default.VideoCall,
                            contentDescription = "Video Call",
                            tint = ElectricCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = PureWhite
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(RoyalBlueCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Profile", color = PureWhite) },
                            leadingIcon = { Icon(Icons.Default.Info, null, tint = ElectricCyan) },
                            onClick = {
                                showMenu = false
                                onOpenProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Chat History", color = CallRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = CallRed) },
                            onClick = {
                                showMenu = false
                                onDeleteChatHistory()
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Privacy Warning Banner if not mutual contacts
            if (!isMutualContact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CallRed.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = CallRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Messaging and calling disabled until both users add each other.",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        message = msg,
                        isFromMe = msg.senderId == currentUserId,
                        onLongPress = { if (isMutualContact) reactionTargetMsg = it },
                        onSwipeToReply = { if (isMutualContact) onSetReplyTo(it) }
                    )
                }
            }

            // Reply Preview Banner
            if (replyToMessage != null && isMutualContact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoyalBlueCard)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Replying to message",
                            fontSize = 11.sp,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = replyToMessage.messageText,
                            fontSize = 13.sp,
                            color = PureWhite,
                            maxLines = 1
                        )
                    }

                    IconButton(onClick = { onSetReplyTo(null) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel reply",
                            tint = PlatinumGray
                        )
                    }
                }
            }

            // Text Input Row (only if mutual contact)
            if (isMutualContact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoyalBlueMedium)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(text = "Message...", color = PlatinumGray.copy(alpha = 0.6f))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RoyalBlueCard,
                            unfocusedContainerColor = RoyalBlueCard,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = GlassmorphismBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .size(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = RoyalBlueDark
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoyalBlueMedium)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔒 Add user back to enable conversation",
                        color = PlatinumGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Long press emoji reaction dialog
        if (reactionTargetMsg != null && isMutualContact) {
            ReactionPicker(
                onDismissRequest = { reactionTargetMsg = null },
                onEmojiSelected = { emoji ->
                    reactionTargetMsg?.let { msg ->
                        onToggleReaction(msg.id, emoji)
                    }
                }
            )
        }
    }
}
