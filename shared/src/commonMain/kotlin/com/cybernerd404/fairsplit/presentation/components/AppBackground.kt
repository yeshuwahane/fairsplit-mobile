package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    applyStatusBarPadding: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == FairSplitColors.DarkBackground
    val gradientBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Deep Slate
                Color(0xFF0A0E17), // Charcoal
                Color(0xFF080C14)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF0FDF4), // Very faint mint tint
                Color(0xFFF8FAFC),
                Color(0xFFFFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Box(
            modifier = if (applyStatusBarPadding) {
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            } else {
                Modifier.fillMaxSize()
            }
        ) {
            content()
        }
    }
}
