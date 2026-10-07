package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.domain.model.TransactionStatus
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun TransactionCard(
    transaction: Transaction,
    currencySymbol: String = "₹",
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isFromMe = transaction.fromMember.isCurrentUser
    val isToMe = transaction.toMember.isCurrentUser

    val iconColor = when {
        isFromMe -> FairSplitColors.NegativeRed
        isToMe -> FairSplitColors.PositiveGreen
        else -> FairSplitColors.AccentIndigo
    }

    val iconVector = when {
        isFromMe -> Icons.Default.ArrowUpward
        isToMe -> Icons.Default.ArrowDownward
        else -> Icons.Default.SyncAlt
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Transfer icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f))
                .border(1.dp, iconColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Epic tag
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = transaction.epicName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "•",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = transaction.time,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Amount and Status Badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$currencySymbol${transaction.amount.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            StatusBadge(status = transaction.status)
        }
    }
}
