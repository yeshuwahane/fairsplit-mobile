package com.cybernerd404.fairsplit.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.cybernerd404.fairsplit.presentation.epics.create.CreateEpicScreen
import com.cybernerd404.fairsplit.presentation.expense.add.AddExpenseScreen
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.presentation.components.*
import com.cybernerd404.fairsplit.presentation.epics.detail.EpicDetailScreen
import com.cybernerd404.fairsplit.presentation.expense.detail.ExpenseDetailScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs

object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Home",
            icon = null
        )

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<HomeViewModel>()
        val overviewState by viewModel.overviewState.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        )

        LaunchedEffect(Unit) {
            viewModel.loadData()
        }

        val listState = rememberLazyListState()

        var showJoinModal by remember { mutableStateOf(false) }

        if (showJoinModal) {
            com.cybernerd404.fairsplit.presentation.epics.JoinEpicModal(
                onDismiss = { showJoinModal = false },
                onJoined = { epic ->
                    showJoinModal = false
                    navigator.push(EpicDetailScreen(epicId = epic.id))
                }
            )
        }

        AppBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                overviewState.isLoading() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                overviewState.isError() -> {
                    ErrorState(
                        message = overviewState.error?.message ?: "Failed to load overview",
                        onRetry = { viewModel.loadData() }
                    )
                }
                overviewState.isSuccess() && overviewState.data != null -> {
                    val data = overviewState.data!!

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // User Header
                        item(key = "home_header") {
                            HomeHeader(userProfile = data.userProfile)
                        }

                        // Relationship Graph: YOU ↔ EPICS (with integrated open typography net balance)
                        item(key = "home_constellation") {
                            HomeConstellationGraph(
                                userProfile = data.userProfile,
                                epics = data.activeEpics,
                                onEpicClick = { epic ->
                                    navigator.push(EpicDetailScreen(epicId = epic.id))
                                },
                                onCreateEpicClick = {
                                    navigator.push(CreateEpicScreen())
                                },
                                onJoinEpicClick = {
                                    showJoinModal = true
                                }
                            )
                        }

                        // Active Epics Carousel / Row
                        item(key = "home_epics_row") {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Active Epics",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${data.activeEpics.size} active",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (data.activeEpics.isEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { navigator.push(CreateEpicScreen()) },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape)
                                                    .background(FairSplitColors.PrimaryGreen.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Groups,
                                                    contentDescription = null,
                                                    tint = FairSplitColors.PrimaryGreen
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Start Your First Epic",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Create a group or join via link to split expenses with friends.",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = FairSplitColors.PrimaryGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp)
                                    ) {
                                        items(data.activeEpics, key = { it.id }) { epic ->
                                            EpicSummaryCard(
                                                epic = epic,
                                                onClick = {
                                                    navigator.push(EpicDetailScreen(epicId = epic.id))
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Recent Activity Section
                        item(key = "home_recent_title") {
                            Text(
                                text = "Recent Activity",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (data.recentExpenses.isEmpty()) {
                            item(key = "home_empty_activity") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { navigator.push(AddExpenseScreen()) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(FairSplitColors.AccentCyan.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ReceiptLong,
                                                contentDescription = null,
                                                tint = FairSplitColors.AccentCyan
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "No Recent Activity",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "When you or friends add expenses, they will appear here in real-time.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = FairSplitColors.AccentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(data.recentExpenses, key = { it.id }) { expense ->
                                ExpenseCard(
                                    expense = expense,
                                    onClick = {
                                        navigator.push(ExpenseDetailScreen(expenseId = expense.id))
                                    }
                                )
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
private fun HomeHeader(userProfile: UserProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back,",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userProfile.name.ifBlank { "Friend" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // User Avatar Circle
        UserAvatar(
            name = userProfile.name,
            avatarUrl = userProfile.avatarUrl,
            avatarInitials = userProfile.avatarInitials,
            size = 46.dp,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun EpicSummaryCard(
    epic: Epic,
    onClick: () -> Unit
) {
    val isOwe = epic.yourBalance < -0.01
    val isGet = epic.yourBalance > 0.01

    val balanceColor = when {
        isOwe -> FairSplitColors.NegativeRed
        isGet -> FairSplitColors.PositiveGreen
        else -> FairSplitColors.SettledGrey
    }

    val balanceText = when {
        isOwe -> "You owe ₹${abs(epic.yourBalance).toInt()}"
        isGet -> "You get ₹${epic.yourBalance.toInt()}"
        else -> "Settled"
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = epic.name.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "₹${epic.totalSpent.toInt()} spent",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${epic.memberCount} members",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(balanceColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = balanceText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
        }
    }
}
