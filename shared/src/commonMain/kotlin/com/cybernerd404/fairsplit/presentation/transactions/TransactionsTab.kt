package com.cybernerd404.fairsplit.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.cybernerd404.fairsplit.domain.model.ActivityEntry
import com.cybernerd404.fairsplit.domain.model.ActivityType
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.EmptyState
import com.cybernerd404.fairsplit.presentation.components.ErrorState
import com.cybernerd404.fairsplit.presentation.components.TransactionCard
import com.cybernerd404.fairsplit.presentation.epics.detail.EpicDetailScreen
import com.cybernerd404.fairsplit.presentation.expense.detail.ExpenseDetailScreen
import com.cybernerd404.fairsplit.presentation.main.LocalRootNavigator
import com.cybernerd404.fairsplit.presentation.transactions.detail.TransactionDetailScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors

object TransactionsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Activity",
            icon = null
        )

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val rootNavigator = LocalRootNavigator.current
        val viewModel = getScreenModel<TransactionsViewModel>()
        val transactionsState by viewModel.transactionsState.collectAsState()
        val activitiesState by viewModel.activitiesState.collectAsState()
        val selectedTab by viewModel.selectedTab.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        )

        val activityListState = rememberLazyListState()
        val settlementsListState = rememberLazyListState()

        LaunchedEffect(Unit) {
            viewModel.loadData()
        }

        val tabs = listOf("All Activity", "Settlements")

        AppBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Activity & History",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Full audit trail of all expenses and settlements",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector: All Activity | Settlements
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { viewModel.setSelectedTab(tab) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == "All Activity") {
                    when {
                        activitiesState.isLoading() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        activitiesState.isError() -> {
                            ErrorState(
                                message = activitiesState.error?.message ?: "Failed to load activity",
                                onRetry = { viewModel.loadData() }
                            )
                        }
                        activitiesState.isSuccess() && activitiesState.data != null -> {
                            val activities = activitiesState.data!!
                            if (activities.isEmpty()) {
                                EmptyState(
                                    title = "No Activity Logged",
                                    message = "All expense additions, edits, deletions, and settlements will appear here."
                                )
                            } else {
                                val grouped = activities.groupBy { it.date }

                                LazyColumn(
                                    state = activityListState,
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
                                    grouped.forEach { (date, items) ->
                                        item(key = "header_act_$date") {
                                            Text(
                                                text = date.uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }

                                        items(items, key = { it.id }) { entry ->
                                            ActivityFeedCard(
                                                entry = entry,
                                                onClick = {
                                                    resolveAndOpenActivity(
                                                        entry = entry,
                                                        onOpenExpense = { expId -> rootNavigator?.push(ExpenseDetailScreen(expenseId = expId)) },
                                                        onOpenTransaction = { txId -> rootNavigator?.push(TransactionDetailScreen(transactionId = txId)) },
                                                        onOpenEpic = { epId -> rootNavigator?.push(EpicDetailScreen(epicId = epId)) }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    when {
                        transactionsState.isLoading() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        transactionsState.isError() -> {
                            ErrorState(
                                message = transactionsState.error?.message ?: "Failed to load transactions",
                                onRetry = { viewModel.loadData() }
                            )
                        }
                        transactionsState.isSuccess() && transactionsState.data != null -> {
                            val transactions = transactionsState.data!!
                            if (transactions.isEmpty()) {
                                EmptyState(
                                    title = "No Settlements",
                                    message = "Settlement records and payments will appear here."
                                )
                            } else {
                                val grouped = transactions.groupBy { it.date }

                                LazyColumn(
                                    state = settlementsListState,
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
                                    grouped.forEach { (date, items) ->
                                        item(key = "header_settle_$date") {
                                            Text(
                                                text = date.uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }

                                        items(items, key = { it.id }) { transaction ->
                                            TransactionCard(
                                                transaction = transaction,
                                                onClick = {
                                                    rootNavigator?.push(TransactionDetailScreen(transactionId = transaction.id))
                                                }
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = FairSplitColors.DarkSurfaceCard,
            contentColor = FairSplitColors.PrimaryGreen
        )
    }
}
}
}

@Composable
private fun ActivityFeedCard(
    entry: ActivityEntry,
    onClick: (() -> Unit)? = null
) {
    val actorColor = Color(entry.actor.avatarColorHex)
    val actionColor = when (entry.type) {
        ActivityType.EXPENSE_ADDED -> FairSplitColors.PrimaryGreen
        ActivityType.EXPENSE_UPDATED -> Color(0xFF6366F1)
        ActivityType.EXPENSE_DELETED -> FairSplitColors.NegativeRed
        ActivityType.SETTLEMENT_RECORDED -> FairSplitColors.PositiveGreen
        ActivityType.SETTLEMENT_DELETED -> FairSplitColors.NegativeRed
        ActivityType.EPIC_CREATED -> FairSplitColors.AccentAmber
        ActivityType.MEMBER_ADDED -> Color(0xFFEC4899)
    }

    val actionIcon = when (entry.type) {
        ActivityType.EXPENSE_ADDED -> Icons.Default.AddCircle
        ActivityType.EXPENSE_UPDATED -> Icons.Default.Edit
        ActivityType.EXPENSE_DELETED -> Icons.Default.Delete
        ActivityType.SETTLEMENT_RECORDED -> Icons.Default.CheckCircle
        ActivityType.SETTLEMENT_DELETED -> Icons.Default.Delete
        ActivityType.EPIC_CREATED -> Icons.Default.Flag
        ActivityType.MEMBER_ADDED -> Icons.Default.PersonAdd
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Actor avatar with action badge
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(actorColor.copy(alpha = 0.15f))
                    .border(1.dp, actorColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.actor.avatarInitials,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = actorColor
                )
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(actionColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Narrative details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.description,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${entry.epicName} • ${entry.timestamp}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (entry.receiptUrl != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Proof attached",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (entry.amount != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "₹${entry.amount.toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (entry.type == ActivityType.SETTLEMENT_RECORDED) FairSplitColors.PositiveGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun resolveAndOpenActivity(
    entry: ActivityEntry,
    onOpenExpense: (String) -> Unit,
    onOpenTransaction: (String) -> Unit,
    onOpenEpic: (String) -> Unit
) {
    when (entry.type) {
        ActivityType.SETTLEMENT_RECORDED -> {
            // 1. Direct transaction ID lookup
            val tx = com.cybernerd404.fairsplit.data.local.MockDataProvider.allTransactions.find {
                it.id == entry.expenseId ||
                (entry.epicId.isNotEmpty() && it.epicId == entry.epicId && entry.amount != null && it.amount == entry.amount) ||
                (entry.amount != null && it.amount == entry.amount)
            }
            if (tx != null) {
                onOpenTransaction(tx.id)
                return
            }
            if (!entry.expenseId.isNullOrEmpty()) {
                val exp = com.cybernerd404.fairsplit.data.local.MockDataProvider.expensesByEpicId.values.flatten().find { it.id == entry.expenseId }
                if (exp != null) {
                    onOpenExpense(exp.id)
                    return
                }
            }
            if (entry.epicId.isNotEmpty()) {
                onOpenEpic(entry.epicId)
            }
        }
        ActivityType.EXPENSE_ADDED, ActivityType.EXPENSE_UPDATED -> {
            // 1. If explicit expenseId is provided and exists in repository
            if (!entry.expenseId.isNullOrEmpty()) {
                val expenseExists = com.cybernerd404.fairsplit.data.local.MockDataProvider.expensesByEpicId.values.any { list ->
                    list.any { it.id == entry.expenseId }
                }
                if (expenseExists) {
                    onOpenExpense(entry.expenseId)
                    return
                }
            }

            // 2. Extract expense title from description (e.g. "added 'chai' ($40)...")
            val titleMatch = Regex("['‘]([^'’]+)['’]").find(entry.description)?.groupValues?.get(1)

            // 3. Search in the epic's expenses by title first, then amount
            val epicExpenses = com.cybernerd404.fairsplit.data.local.MockDataProvider.expensesByEpicId[entry.epicId] ?: emptyList()
            val matchedInEpic = if (!titleMatch.isNullOrEmpty()) {
                epicExpenses.find { it.title.equals(titleMatch, ignoreCase = true) }
            } else if (entry.amount != null) {
                epicExpenses.find { it.amount == entry.amount }
            } else null

            if (matchedInEpic != null) {
                onOpenExpense(matchedInEpic.id)
                return
            }

            // 4. Search across all epics
            val allExpenses = com.cybernerd404.fairsplit.data.local.MockDataProvider.expensesByEpicId.values.flatten()
            val globalMatched = if (!titleMatch.isNullOrEmpty()) {
                allExpenses.find { it.title.equals(titleMatch, ignoreCase = true) }
            } else if (entry.amount != null) {
                allExpenses.find { it.amount == entry.amount }
            } else null

            if (globalMatched != null) {
                onOpenExpense(globalMatched.id)
                return
            }

            // Fallback to Epic screen only if expense is not found anywhere
            if (entry.epicId.isNotEmpty()) {
                onOpenEpic(entry.epicId)
            }
        }
        ActivityType.EPIC_CREATED, ActivityType.MEMBER_ADDED,
        ActivityType.EXPENSE_DELETED, ActivityType.SETTLEMENT_DELETED -> {
            if (entry.epicId.isNotEmpty()) {
                onOpenEpic(entry.epicId)
            }
        }
    }
}
