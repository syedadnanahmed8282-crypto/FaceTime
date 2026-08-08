package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FaceTimeColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = RoyalBlueDark,
    primaryContainer = RoyalBlueCard,
    onPrimaryContainer = PureWhite,
    secondary = ElectricCyanVariant,
    onSecondary = PureWhite,
    background = RoyalBlueDark,
    onBackground = PureWhite,
    surface = RoyalBlueSurface,
    onSurface = PureWhite,
    surfaceVariant = RoyalBlueMedium,
    onSurfaceVariant = PlatinumGray,
    outline = GlassmorphismBorder,
    error = CallRed,
    onError = PureWhite
)

@Composable
fun FaceTimeTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = FaceTimeColorScheme,
        typography = Typography,
        content = content
    )
}

