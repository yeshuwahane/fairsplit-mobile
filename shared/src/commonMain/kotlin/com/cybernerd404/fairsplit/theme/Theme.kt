package com.cybernerd404.fairsplit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FairSplitColors.PrimaryGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),

    secondary = FairSplitColors.AccentIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFC7D2FE),

    tertiary = FairSplitColors.AccentAmber,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFDE68A),

    background = FairSplitColors.DarkBackground,
    onBackground = FairSplitColors.DarkTextPrimary,

    surface = FairSplitColors.DarkSurface,
    onSurface = FairSplitColors.DarkTextPrimary,
    surfaceVariant = FairSplitColors.DarkSurfaceVariant,
    onSurfaceVariant = FairSplitColors.DarkTextSecondary,

    outline = FairSplitColors.DarkCardBorder,
    outlineVariant = Color(0xFF21262D),

    error = FairSplitColors.NegativeRed,
    onError = Color.White,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFECACA)
)

private val LightColorScheme = lightColorScheme(
    primary = FairSplitColors.PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),

    secondary = FairSplitColors.AccentIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF3730A3),

    tertiary = FairSplitColors.AccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),

    background = FairSplitColors.LightBackground,
    onBackground = FairSplitColors.LightTextPrimary,

    surface = FairSplitColors.LightSurface,
    onSurface = FairSplitColors.LightTextPrimary,
    surfaceVariant = FairSplitColors.LightSurfaceVariant,
    onSurfaceVariant = FairSplitColors.LightTextSecondary,

    outline = FairSplitColors.LightCardBorder,
    outlineVariant = Color(0xFFE5E7EB),

    error = FairSplitColors.NegativeRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

@Composable
fun FairSplitTheme(
    darkTheme: Boolean = true, // default to premium dark theme matching GTA6Insider
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
