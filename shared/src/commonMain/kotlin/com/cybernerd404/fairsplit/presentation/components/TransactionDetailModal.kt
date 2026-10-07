package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Transaction
import com.cybernerd404.fairsplit.theme.FairSplitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailModal(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onOpenEpic: ((String) -> Unit)? = null
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isFromMe = transaction.fromMember.isCurrentUser
    val isToMe = transaction.toMember.isCurrentUser

    val headerColor = when {
        isFromMe -> FairSplitColors.NegativeRed
        isToMe -> FairSplitColors.PositiveGreen
        else -> FairSplitColors.AccentIndigo
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settlement Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Highlight Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = headerColor.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, headerColor.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "₹${transaction.amount.toInt()}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = headerColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusBadge(status = transaction.status)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Parties Flow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Payer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(transaction.fromMember.avatarColorHex).copy(alpha = 0.2f))
                            .border(1.5.dp, Color(transaction.fromMember.avatarColorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.fromMember.avatarInitials,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(transaction.fromMember.avatarColorHex)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (transaction.fromMember.isCurrentUser) "You (Payer)" else transaction.fromMember.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Arrow
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = FairSplitColors.PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "paid",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Receiver
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(transaction.toMember.avatarColorHex).copy(alpha = 0.2f))
                            .border(1.5.dp, Color(transaction.toMember.avatarColorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.toMember.avatarInitials,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(transaction.toMember.avatarColorHex)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (transaction.toMember.isCurrentUser) "You (Receiver)" else transaction.toMember.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Info rows
            DetailInfoRow(label = "Epic", value = transaction.epicName)
            DetailInfoRow(label = "Date & Time", value = "${transaction.date} • ${transaction.time}")
            if (transaction.subtitle.isNotEmpty()) {
                DetailInfoRow(label = "Note", value = transaction.subtitle)
            }
            DetailInfoRow(label = "Transaction ID", value = transaction.id)

            if (transaction.epicId.isNotEmpty() && onOpenEpic != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        onDismiss()
                        onOpenEpic(transaction.epicId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open '${transaction.epicName}' Epic",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
