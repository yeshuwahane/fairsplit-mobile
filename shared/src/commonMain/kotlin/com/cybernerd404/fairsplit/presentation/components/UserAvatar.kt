package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun UserAvatar(
    name: String,
    avatarUrl: String?,
    avatarInitials: String = "",
    size: Dp = 48.dp,
    fontSize: TextUnit = 16.sp,
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val initials = if (avatarInitials.isNotBlank()) avatarInitials else name.take(2).uppercase().ifEmpty { "ME" }
    val isEmoji = avatarUrl?.startsWith("avatar_") == true || (avatarUrl != null && avatarUrl.length <= 4 && !avatarUrl.contains("/") && !avatarUrl.contains(":"))
    val isImage = avatarUrl != null && !isEmoji && (
        avatarUrl.startsWith("http://") || 
        avatarUrl.startsWith("https://") || 
        avatarUrl.startsWith("content://") || 
        avatarUrl.startsWith("file://") || 
        avatarUrl.startsWith("/")
    )

    val emojiMap = mapOf(
        "avatar_traveler" to "🧳",
        "avatar_foodie" to "🍕",
        "avatar_gamer" to "🎮",
        "avatar_coder" to "💻",
        "avatar_explorer" to "🚀",
        "avatar_artist" to "🎨",
        "avatar_party" to "🎉",
        "avatar_star" to "⭐"
    )

    Box(
        modifier = Modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(FairSplitColors.PrimaryGreen.copy(alpha = 0.2f))
                .border(1.5.dp, FairSplitColors.PrimaryGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                isImage -> {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                isEmoji -> {
                    val emoji = emojiMap[avatarUrl] ?: avatarUrl ?: "👤"
                    Text(
                        text = emoji,
                        fontSize = (fontSize.value * 1.3).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> {
                    Text(
                        text = initials,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = FairSplitColors.PrimaryGreen
                    )
                }
            }
        }

        if (showEditBadge) {
            val badgeSize = (size.value * 0.36).dp.coerceAtLeast(24.dp)
            val iconSize = (badgeSize.value * 0.55).dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(1.dp, FairSplitColors.PrimaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit Avatar",
                    tint = FairSplitColors.PrimaryGreen,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
