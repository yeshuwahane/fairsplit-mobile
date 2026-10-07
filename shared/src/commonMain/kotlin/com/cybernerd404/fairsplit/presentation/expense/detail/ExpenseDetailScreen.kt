package com.cybernerd404.fairsplit.presentation.expense.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.ErrorState
import com.cybernerd404.fairsplit.presentation.components.ReceiptViewerModal
import com.cybernerd404.fairsplit.presentation.components.SwipeBackContainer
import com.cybernerd404.fairsplit.presentation.expense.add.AddExpenseScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors
import org.koin.core.parameter.parametersOf

data class ExpenseDetailScreen(val expenseId: String) : Screen {

    override val key: ScreenKey = "ExpenseDetailScreen_$expenseId"

    @Composable
    override fun Content() {
        val viewModel = getScreenModel<ExpenseDetailViewModel> { parametersOf(expenseId) }
        val expenseState by viewModel.expenseState.collectAsState()
        val showReceiptModal by viewModel.showReceiptModal.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(expenseId) {
            viewModel.loadExpense(expenseId)
        }

        SwipeBackContainer(onPop = { navigator.pop() }) {
            AppBackground {
                when {
                expenseState.isLoading() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                expenseState.isError() -> {
                    ErrorState(
                        message = expenseState.error?.message ?: "Failed to load expense details",
                        onRetry = { viewModel.loadExpense() }
                    )
                }
                expenseState.isSuccess() && expenseState.data != null -> {
                    val expense = expenseState.data!!
                    val isSettlement = expense.isSettlement
                    val categoryColor = if (isSettlement) FairSplitColors.PositiveGreen else Color(expense.category.colorHex)

                    if (showReceiptModal) {
                        ReceiptViewerModal(
                            expense = expense,
                            onDismiss = { viewModel.closeReceipt() }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Top bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { navigator.pop() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row {
                                if (!isSettlement) {
                                    IconButton(
                                        onClick = {
                                            navigator.push(
                                                AddExpenseScreen(
                                                    initialEpicId = expense.epicId,
                                                    existingExpenseId = expense.id
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteExpense {
                                            navigator.pop()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(FairSplitColors.NegativeRed.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = FairSplitColors.NegativeRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Amount Card Header
                        val receiver = expense.participants.firstOrNull() ?: expense.paidBy
                        val payerName = if (expense.paidBy.isCurrentUser) "${expense.paidBy.name} (You)" else expense.paidBy.name
                        val receiverName = if (receiver.isCurrentUser) "${receiver.name} (You)" else receiver.name

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (isSettlement) FairSplitColors.PositiveGreen.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.dp,
                                    if (isSettlement) FairSplitColors.PositiveGreen.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(categoryColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isSettlement) "SETTLEMENT PAYMENT" else expense.category.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (isSettlement) "$payerName → $receiverName" else expense.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "₹${expense.amount.toInt()}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSettlement) FairSplitColors.PositiveGreen else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${expense.epicName} • ${expense.date}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Payer & Impact Card (or Settlement Details Card)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                if (isSettlement) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Paid by",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = payerName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = FairSplitColors.PositiveGreen,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Paid to",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = receiverName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Paid by",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (expense.paidBy.isCurrentUser) "You (${expense.paidBy.name})" else expense.paidBy.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Your share",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "₹${expense.yourShare.toInt()}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FairSplitColors.PrimaryGreen
                                            )
                                        }
                                    }

                                    if (!expense.paidBy.isCurrentUser) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "You owe ${expense.paidBy.name}",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "₹${expense.yourShare.toInt()}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FairSplitColors.NegativeRed
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Split Breakdown Section (for regular expenses)
                        if (!isSettlement) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "SPLIT BETWEEN (${expense.shares.size} MEMBERS)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    expense.shares.forEach { share ->
                                        val mColor = Color(share.member.avatarColorHex)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                com.cybernerd404.fairsplit.presentation.components.UserAvatar(
                                                    name = share.member.name,
                                                    avatarUrl = share.member.avatarUrl,
                                                    avatarInitials = share.member.avatarInitials,
                                                    size = 32.dp,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = if (share.member.isCurrentUser) "${share.member.name} (You)" else share.member.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Text(
                                                text = "₹${share.shareAmount.toInt()}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Activity & History Trail Card
                        Spacer(modifier = Modifier.height(18.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "ACTIVITY HISTORY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    com.cybernerd404.fairsplit.presentation.components.UserAvatar(
                                        name = expense.createdBy.name,
                                        avatarUrl = expense.createdBy.avatarUrl,
                                        avatarInitials = expense.createdBy.avatarInitials,
                                        size = 34.dp,
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        val creatorName = if (expense.createdBy.isCurrentUser) "${expense.createdBy.name} (You)" else expense.createdBy.name
                                        val actionWord = if (isSettlement) "Recorded settlement" else "Added expense"
                                        Text(
                                            text = "$actionWord by $creatorName",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                         Text(
                                            text = "${expense.date} • ${expense.createdAtTime}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Receipt Section (if attached)
                        if (expense.receiptUrl != null) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.openReceipt() },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ReceiptLong,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Receipt Attached",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Tap to view full receipt",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Optional Note
                        if (!expense.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "NOTE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = expense.note,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
}
