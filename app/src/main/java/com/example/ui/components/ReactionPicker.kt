package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GlassmorphismBorder
import com.example.ui.theme.RoyalBlueCard

@Composable
fun ReactionPicker(
    onDismissRequest: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf("❤️", "👍", "😂", "😮", "😢")

    Dialog(onDismissRequest = onDismissRequest) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(RoyalBlueCard)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            emojis.forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .clickable {
                            onEmojiSelected(emoji)
                            onDismissRequest()
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}
