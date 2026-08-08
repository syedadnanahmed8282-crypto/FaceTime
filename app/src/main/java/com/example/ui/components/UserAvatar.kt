package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CallGreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueMedium

@Composable
fun UserAvatar(
    photoUrl: String?,
    displayName: String,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    showStatusDot: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "$displayName Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape)
            )
        } else {
            val initial = displayName.take(1).uppercase().ifEmpty { "F" }
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(RoyalBlueMedium)
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = ElectricCyan,
                    fontSize = (size.value * 0.4).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showStatusDot) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) CallGreen else Color.Gray)
                    .border(2.dp, RoyalBlueDark, CircleShape)
            )
        }
    }
}
