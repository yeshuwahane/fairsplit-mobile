package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Member

@Composable
fun MemberContributionBar(
    member: Member,
    maxContribution: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val ratio = if (maxContribution > 0) (member.totalSpent / maxContribution).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) ratio else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    val avatarColor = Color(member.avatarColorHex)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle
        UserAvatar(
            name = member.name,
            avatarUrl = member.avatarUrl,
            avatarInitials = member.avatarInitials,
            size = 38.dp,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name + Progress bar + Amount
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (member.isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(You)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "$currencySymbol${member.totalSpent.toInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(avatarColor)
                )
            }
        }
    }
}
