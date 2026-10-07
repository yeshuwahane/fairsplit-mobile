package com.cybernerd404.fairsplit.presentation.epics.detail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.presentation.components.*
import com.cybernerd404.fairsplit.presentation.epics.create.CreateEpicScreen
import com.cybernerd404.fairsplit.presentation.expense.add.AddExpenseScreen
import com.cybernerd404.fairsplit.presentation.expense.detail.ExpenseDetailScreen
import com.cybernerd404.fairsplit.presentation.transactions.detail.TransactionDetailScreen
import com.cybernerd404.fairsplit.presentation.transactions.resolveAndOpenActivity
import com.cybernerd404.fairsplit.presentation.epics.detail.EpicInviteSheet
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

data class EpicDetailScreen(val epicId: String) : Screen {

    override val key: ScreenKey = "EpicDetailScreen_$epicId"

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<EpicDetailViewModel> { parametersOf(epicId) }
        val epicState by viewModel.epicState.collectAsState()
        val expensesState by viewModel.expensesState.collectAsState()
        val selectedTab by viewModel.selectedTab.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh(epicId) }
        )


        var showSettleModal by remember { mutableStateOf(false) }
        var settleDebtor by remember { mutableStateOf<com.cybernerd404.fairsplit.domain.model.Member?>(null) }
        var settleCreditor by remember { mutableStateOf<com.cybernerd404.fairsplit.domain.model.Member?>(null) }
        var settleAmount by remember { mutableStateOf<Double?>(null) }
        var showInviteSheet by remember { mutableStateOf(false) }
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }

        SwipeBackContainer(onPop = { navigator.pop() }) {
            AppBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { navigator.push(AddExpenseScreen(initialEpicId = epicId)) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense"
                            )
                        }
                    }
                ) { paddingValues ->
                when {
                    epicState.isLoading() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    epicState.isError() -> {
                        ErrorState(
                            message = epicState.error?.message ?: "Failed to load epic details",
                            onRetry = { viewModel.loadData() }
                        )
                    }
                    epicState.isSuccess() && epicState.data != null -> {
                        val epic = epicState.data!!
                        val expenses = expensesState.data ?: epic.recentExpenses

                        val activitiesState by viewModel.activitiesState.collectAsState()

                        if (showSettleModal) {
                            SettleUpModal(
                                epic = epic,
                                preselectedDebtor = settleDebtor,
                                preselectedCreditor = settleCreditor,
                                initialAmount = settleAmount,
                                onDismiss = {
                                    showSettleModal = false
                                    settleDebtor = null
                                    settleCreditor = null
                                    settleAmount = null
                                },
                                onConfirmSettlement = { payer, receiver, amount, note, receiptUrl ->
                                    viewModel.settleDebt(payer, receiver, amount, note, receiptUrl)
                                }
                            )
                        }

                        if (showInviteSheet) {
                            EpicInviteSheet(
                                epic = epic,
                                onDismiss = { showInviteSheet = false }
                            )
                        }

                        if (showDeleteConfirmDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmDialog = false },
                                title = {
                                    Text(
                                        text = "Delete Group?",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Are you sure you want to delete '${epic.name}'? All expenses, settlements, and activity will be removed.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showDeleteConfirmDialog = false
                                            viewModel.deleteEpic {
                                                navigator.pop()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = FairSplitColors.NegativeRed,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Top App Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { navigator.pop() },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = epic.name,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${epic.memberCount} members • ₹${epic.totalSpent.toInt()} total",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { showInviteSheet = true },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share Invite Link",
                                            tint = FairSplitColors.AccentCyan
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            settleDebtor = null
                                            settleCreditor = null
                                            settleAmount = null
                                            showSettleModal = true
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = "Settle Up",
                                            tint = FairSplitColors.PositiveGreen
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { showDeleteConfirmDialog = true },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Group",
                                            tint = FairSplitColors.NegativeRed
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val selectedTab by viewModel.selectedTab.collectAsState()

                            // Segmented Navigation: Overview | Expenses | Balances | Activity
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(4.dp)
                            ) {
                                EpicDetailTab.entries.forEach { tab ->
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
                                            text = tab.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Instant Tab Content
                            when (selectedTab) {
                                EpicDetailTab.OVERVIEW -> {
                                    OverviewTabContent(
                                        epic = epic,
                                        onExpenseClick = { expenseId ->
                                            navigator.push(ExpenseDetailScreen(expenseId = expenseId))
                                        },
                                        onSettleClick = { payer, receiver, amount ->
                                            settleDebtor = payer
                                            settleCreditor = receiver
                                            settleAmount = amount
                                            showSettleModal = true
                                        }
                                    )
                                }
                                EpicDetailTab.EXPENSES -> {
                                    ExpensesTabContent(
                                        expenses = expenses,
                                        onExpenseClick = { expenseId ->
                                            navigator.push(ExpenseDetailScreen(expenseId = expenseId))
                                        }
                                    )
                                }
                                EpicDetailTab.BALANCES -> {
                                    BalancesTabContent(
                                        epic = epic,
                                        expenses = expenses,
                                        onSettleClick = { settlement ->
                                            settleDebtor = settlement.fromMember
                                            settleCreditor = settlement.toMember
                                            settleAmount = settlement.amount
                                            showSettleModal = true
                                        },
                                        onSettledExpenseClick = { expense ->
                                            val tx = com.cybernerd404.fairsplit.data.local.MockDataProvider.allTransactions.find {
                                                it.epicId == epic.id && it.amount == expense.amount &&
                                                it.fromMember.id == expense.paidBy.id
                                            }
                                            val targetTxId = tx?.id ?: expense.id
                                            navigator.push(TransactionDetailScreen(transactionId = targetTxId))
                                        }
                                    )
                                }
                                EpicDetailTab.ACTIVITY -> {
                                    ActivityTabContent(
                                        activities = activitiesState.data ?: emptyList(),
                                        onExpenseClick = { expenseId ->
                                            navigator.push(ExpenseDetailScreen(expenseId = expenseId))
                                        },
                                        onTransactionClick = { transactionId ->
                                            navigator.push(TransactionDetailScreen(transactionId = transactionId))
                                        }
                                    )
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
}

@Composable
private fun OverviewTabContent(
    epic: Epic,
    onExpenseClick: (String) -> Unit,
    onSettleClick: (payer: com.cybernerd404.fairsplit.domain.model.Member?, receiver: com.cybernerd404.fairsplit.domain.model.Member?, amount: Double?) -> Unit
) {
    val maxContribution = epic.members.maxOfOrNull { it.totalSpent } ?: 1.0
    val othersSpent = epic.totalSpent - epic.yourSpent
    val isOwe = epic.yourBalance < -0.01
    val isGet = epic.yourBalance > 0.01

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
    ) {
        // 1. Epic Overview Hero Card (Clean alignment, no cut-offs)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Top Labels Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL SPENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "YOUR BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Big Values Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${epic.totalSpent.toInt()}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = when {
                                isOwe -> "-₹${abs(epic.yourBalance).toInt()}"
                                isGet -> "+₹${epic.yourBalance.toInt()}"
                                else -> "₹0"
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isOwe -> FairSplitColors.NegativeRed
                                isGet -> FairSplitColors.PositiveGreen
                                else -> FairSplitColors.SettledGrey
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Submetrics 3-Column Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "You paid",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${epic.yourSpent.toInt()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FairSplitColors.PrimaryGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Others paid",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${othersSpent.toInt()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Members",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${epic.memberCount} people",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. "WHO SHOULD PAY NEXT" Directed Flow Graph
        item {
            WhoShouldPayNextBubbleGraph(
                epic = epic,
                onSettleClick = onSettleClick
            )
        }

        // 3. Spending Categories Donut Ring
        item {
            EpicCategoryDonutRing(
                breakdowns = epic.categoryBreakdowns,
                totalSpent = epic.totalSpent
            )
        }

        // 4. Invite & Friends Card
        item {
            EpicInviteCard(epic = epic)
        }

        // 5. Member Contributions Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "MEMBER CONTRIBUTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    epic.members.sortedByDescending { it.totalSpent }.forEach { member ->
                        MemberContributionBar(
                            member = member,
                            maxContribution = maxContribution
                        )
                    }
                }
            }
        }

        // 5. Recent Expenses
        item {
            Column {
                Text(
                    text = "Recent Transactions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                epic.recentExpenses.take(3).forEach { expense ->
                    ExpenseCard(
                        expense = expense,
                        onClick = { onExpenseClick(expense.id) },
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpensesTabContent(
    expenses: List<Expense>,
    onExpenseClick: (String) -> Unit
) {
    val grouped = expenses.groupBy { it.date }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
    ) {
        grouped.forEach { (date, itemsForDate) ->
            item {
                Text(
                    text = date.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(itemsForDate) { expense ->
                ExpenseCard(
                    expense = expense,
                    onClick = { onExpenseClick(expense.id) }
                )
            }
        }
    }
}

@Composable
private fun BalancesTabContent(
    epic: Epic,
    expenses: List<Expense>,
    onSettleClick: (com.cybernerd404.fairsplit.domain.model.Settlement) -> Unit,
    onSettledExpenseClick: (Expense) -> Unit = {}
) {
    val pendingSettlements = epic.settlements.filter { it.amount > 0.01 }
        .sortedWith(compareByDescending<com.cybernerd404.fairsplit.domain.model.Settlement> { it.fromMember.isCurrentUser || it.toMember.isCurrentUser }
            .thenByDescending { it.amount })
    val settledExpenses = expenses.filter {
        it.isSettlement ||
        it.title.startsWith("Settlement:", ignoreCase = true) ||
        (it.category == com.cybernerd404.fairsplit.domain.model.ExpenseCategory.OTHER && it.participants.size == 1 && it.participants[0].id != it.paidBy.id)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
    ) {
        // 1. Your balance summary
        item {
            PositionCard(
                youSpent = epic.yourSpent,
                yourBalance = epic.yourBalance
            )
        }

        // 2. Settlement Visualization: Directed money flow between members
        if (pendingSettlements.isNotEmpty()) {
            item {
                SettlementFlowVisualization(settlements = pendingSettlements)
            }
        }

        // 3. Pending Settlement Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PENDING SETTLEMENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (pendingSettlements.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(FairSplitColors.PositiveGreen.copy(alpha = 0.12f))
                            .border(1.dp, FairSplitColors.PositiveGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FairSplitColors.PositiveGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "All debts are settled!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FairSplitColors.PositiveGreen
                            )
                            Text(
                                text = "No pending payments in this Epic",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    pendingSettlements.forEach { settlement ->
                        SettlementFlowCard(
                            settlement = settlement,
                            onSettleClick = onSettleClick
                        )
                    }
                }
            }
        }

        // 4. Settled Debts History
        if (settledExpenses.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SETTLED DEBTS & PAYMENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    settledExpenses.forEach { expense ->
                        SettledDebtHistoryCard(
                            expense = expense,
                            onClick = { onSettledExpenseClick(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettledDebtHistoryCard(
    expense: Expense,
    onClick: () -> Unit = {}
) {
    val payer = expense.paidBy
    val receiver = expense.participants.firstOrNull() ?: payer
    val payerName = if (payer.isCurrentUser) "You" else payer.name
    val receiverName = if (receiver.isCurrentUser) "You" else receiver.name
    val creatorName = if (expense.createdBy.isCurrentUser) "You" else expense.createdBy.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FairSplitColors.PositiveGreen.copy(alpha = 0.06f))
            .border(1.dp, FairSplitColors.PositiveGreen.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FairSplitColors.PositiveGreen.copy(alpha = 0.15f))
                    .border(1.dp, FairSplitColors.PositiveGreen.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Settled",
                    tint = FairSplitColors.PositiveGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$payerName paid $receiverName",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${expense.date} • Recorded by $creatorName",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${expense.amount.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FairSplitColors.PositiveGreen
            )
            Text(
                text = "Settled",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = FairSplitColors.PositiveGreen
            )
        }
    }
}

@Composable
private fun ActivityTabContent(
    activities: List<com.cybernerd404.fairsplit.domain.model.ActivityEntry>,
    onExpenseClick: (String) -> Unit = {},
    onTransactionClick: (String) -> Unit = {}
) {
    if (activities.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No activity recorded yet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Every expense, edit, deletion, and settlement will appear here.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        val grouped = activities.groupBy { it.date }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
        ) {
            grouped.forEach { (date, itemsForDate) ->
                item {
                    Text(
                        text = date.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(itemsForDate) { entry ->
                    ActivityEntryCard(
                        entry = entry,
                        onClick = {
                            resolveAndOpenActivity(
                                entry = entry,
                                onOpenExpense = onExpenseClick,
                                onOpenTransaction = onTransactionClick,
                                onOpenEpic = {}
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityEntryCard(
    entry: com.cybernerd404.fairsplit.domain.model.ActivityEntry,
    onClick: (() -> Unit)? = null
) {
    val actorColor = Color(entry.actor.avatarColorHex)
    val actionColor = when (entry.type) {
        com.cybernerd404.fairsplit.domain.model.ActivityType.EXPENSE_ADDED -> FairSplitColors.PrimaryGreen
        com.cybernerd404.fairsplit.domain.model.ActivityType.EXPENSE_UPDATED -> Color(0xFF6366F1)
        com.cybernerd404.fairsplit.domain.model.ActivityType.EXPENSE_DELETED -> FairSplitColors.NegativeRed
        com.cybernerd404.fairsplit.domain.model.ActivityType.SETTLEMENT_RECORDED -> FairSplitColors.PositiveGreen
        com.cybernerd404.fairsplit.domain.model.ActivityType.SETTLEMENT_DELETED -> FairSplitColors.NegativeRed
        com.cybernerd404.fairsplit.domain.model.ActivityType.EPIC_CREATED -> FairSplitColors.AccentAmber
        com.cybernerd404.fairsplit.domain.model.ActivityType.MEMBER_ADDED -> Color(0xFFEC4899)
    }

    val actionIcon = when (entry.type) {
        com.cybernerd404.fairsplit.domain.model.ActivityType.EXPENSE_ADDED -> Icons.Default.AddCircle
        com.cybernerd404.fairsplit.domain.model.ActivityType.EXPENSE_UPDATED -> Icons.Default.Edit
        com.cybernerd404.fairsplit.domain.model.ActivityType.EXPENSE_DELETED -> Icons.Default.Delete
        com.cybernerd404.fairsplit.domain.model.ActivityType.SETTLEMENT_RECORDED -> Icons.Default.CheckCircle
        com.cybernerd404.fairsplit.domain.model.ActivityType.SETTLEMENT_DELETED -> Icons.Default.Delete
        com.cybernerd404.fairsplit.domain.model.ActivityType.EPIC_CREATED -> Icons.Default.Flag
        com.cybernerd404.fairsplit.domain.model.ActivityType.MEMBER_ADDED -> Icons.Default.PersonAdd
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Actor avatar with action badge
        Box(contentAlignment = Alignment.BottomEnd) {
            com.cybernerd404.fairsplit.presentation.components.UserAvatar(
                name = entry.actor.name,
                avatarUrl = entry.actor.avatarUrl,
                avatarInitials = entry.actor.avatarInitials,
                size = 42.dp,
                fontSize = 13.sp
            )

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
                    text = entry.timestamp,
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
                color = if (entry.type == com.cybernerd404.fairsplit.domain.model.ActivityType.SETTLEMENT_RECORDED) FairSplitColors.PositiveGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EpicInviteCard(epic: Epic) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val shareLauncher = rememberShareLauncher()
    val inviteCode = epic.inviteCode.ifBlank { "FS-${epic.name.filter { it.isLetter() }.take(4).uppercase().ifEmpty { "EPIC" }}-${epic.id.takeLast(4)}" }
    val inviteUrl = "https://yeshuwahane.github.io/fairsplit/join?code=$inviteCode"
    var copiedCode by remember { mutableStateOf(false) }
    var copiedLink by remember { mutableStateOf(false) }

    LaunchedEffect(copiedCode) {
        if (copiedCode) {
            kotlinx.coroutines.delay(2000)
            copiedCode = false
        }
    }
    LaunchedEffect(copiedLink) {
        if (copiedLink) {
            kotlinx.coroutines.delay(2000)
            copiedLink = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, FairSplitColors.PrimaryGreen.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = FairSplitColors.PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INVITE FRIENDS & COLLABORATORS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = FairSplitColors.PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Share this code or link with friends to let them join this Epic instantly. Anyone who joins will automatically become your friend for future splits!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Monospace Invite Code Row
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INVITE CODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = inviteCode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FairSplitColors.AccentCyan,
                            letterSpacing = 1.5.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(inviteCode))
                            copiedCode = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (copiedCode) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copiedCode) "Copied!" else "Copy Code",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Copy Web Link & Share Invite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(inviteUrl))
                        copiedLink = true
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (copiedLink) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = if (copiedLink) Icons.Default.Check else Icons.Default.Link,
                        contentDescription = "Copy Link",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (copiedLink) "Link Copied!" else "Copy Link",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        val shareMessage = "🎉 You're invited to join '${epic.name}' on FairSplit!\n\nTap to join directly:\n$inviteUrl\n\nOr open FairSplit and enter code: $inviteCode"
                        shareLauncher.share(shareMessage, "Invite to ${epic.name}")
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FairSplitColors.PrimaryGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share Invite",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


