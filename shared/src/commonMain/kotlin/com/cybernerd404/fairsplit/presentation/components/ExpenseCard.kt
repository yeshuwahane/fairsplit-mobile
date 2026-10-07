package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory

@Composable
fun ExpenseCard(
    expense: Expense,
    currencySymbol: String = "₹",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSettlement = expense.isSettlement
    val categoryColor = if (isSettlement) com.cybernerd404.fairsplit.theme.FairSplitColors.PositiveGreen else Color(expense.category.colorHex)
    val categoryIcon = if (isSettlement) {
        Icons.Default.CheckCircle
    } else {
        when (expense.category) {
            ExpenseCategory.FOOD -> Icons.Default.Restaurant
            ExpenseCategory.STAY -> Icons.Default.Hotel
            ExpenseCategory.TRAVEL -> Icons.Default.DirectionsCar
            ExpenseCategory.ACTIVITIES -> Icons.Default.Surfing
            ExpenseCategory.SHOPPING -> Icons.Default.ShoppingBag
            ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
            ExpenseCategory.OTHER -> Icons.Default.Receipt
        }
    }

    val cardBg = if (isSettlement) {
        com.cybernerd404.fairsplit.theme.FairSplitColors.PositiveGreen.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardBorder = if (isSettlement) {
        com.cybernerd404.fairsplit.theme.FairSplitColors.PositiveGreen.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category / Settlement Icon Circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.15f))
                .border(1.dp, categoryColor.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = if (isSettlement) "Settlement" else expense.category.displayName,
                tint = categoryColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Payer info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSettlement) {
                        val receiver = expense.participants.firstOrNull() ?: expense.paidBy
                        val payerName = if (expense.paidBy.isCurrentUser) "You" else expense.paidBy.name
                        val receiverName = if (receiver.isCurrentUser) "You" else receiver.name
                        "$payerName paid $receiverName"
                    } else {
                        expense.title
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (expense.receiptUrl != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Receipt attached",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            if (isSettlement) {
                val createdByName = if (expense.createdBy.isCurrentUser) "You" else expense.createdBy.name
                Text(
                    text = "Recorded by $createdByName • Debt Settle",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            } else {
                val payerText = if (expense.paidBy.isCurrentUser) "You paid" else "${expense.paidBy.name} paid"
                val createdBySuffix = if (expense.createdBy.id != expense.paidBy.id) {
                    val creator = if (expense.createdBy.isCurrentUser) "You" else expense.createdBy.name
                    " • Added by $creator"
                } else ""
                Text(
                    text = "$payerText • ${expense.participants.size} people$createdBySuffix",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        // Amount & Share / Status
        val isUserInvolved = expense.paidBy.isCurrentUser || expense.participants.any { it.isCurrentUser }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = "$currencySymbol${expense.amount.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSettlement) com.cybernerd404.fairsplit.theme.FairSplitColors.PositiveGreen else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when {
                    isSettlement -> "Settled"
                    !isUserInvolved -> "Not involved"
                    else -> "Your share: $currencySymbol${expense.yourShare.toInt()}"
                },
                fontSize = 11.sp,
                fontWeight = if (isSettlement || !isUserInvolved) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isSettlement -> com.cybernerd404.fairsplit.theme.FairSplitColors.PositiveGreen
                    !isUserInvolved -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
