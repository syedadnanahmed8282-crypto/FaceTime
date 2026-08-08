package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanVariant
import com.example.ui.theme.PlatinumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlueCard
import com.example.ui.theme.RoyalBlueDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromMe: Boolean,
    onLongPress: (ChatMessage) -> Unit,
    onSwipeToReply: (ChatMessage) -> Unit
) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(message.timestamp))

    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    val bubbleBg = if (isFromMe) ElectricCyanVariant.copy(alpha = 0.25f) else RoyalBlueCard
    val borderClr = if (isFromMe) ElectricCyan.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .border(1.dp, borderClr, bubbleShape)
                    .combinedClickable(
                        onClick = { onSwipeToReply(message) },
                        onLongClick = { onLongPress(message) }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    // Reply preview box
                    if (!message.replyToText.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RoyalBlueDark.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Replying: ${message.replyToText}",
                                fontSize = 12.sp,
                                color = ElectricCyan,
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = message.messageText,
                        color = PureWhite,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            color = PlatinumGray.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )

                        if (isFromMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val isDelivered = message.status == ChatMessage.STATUS_DELIVERED
                            Icon(
                                imageVector = if (isDelivered) Icons.Default.DoneAll else Icons.Default.Check,
                                contentDescription = if (isDelivered) "Delivered" else "Sent",
                                tint = if (isDelivered) ElectricCyan else PlatinumGray,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Reactions pill if any
            if (message.reactions.isNotEmpty()) {
                val emojiSummary = message.reactions.values.joinToString(" ")
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalBlueDark)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = emojiSummary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
