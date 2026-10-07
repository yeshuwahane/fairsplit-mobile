package com.cybernerd404.fairsplit.presentation.epics

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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.EpicStatus
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.EmptyState
import com.cybernerd404.fairsplit.presentation.components.ErrorState
import com.cybernerd404.fairsplit.presentation.epics.create.CreateEpicScreen
import com.cybernerd404.fairsplit.presentation.epics.detail.EpicDetailScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs

object EpicsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Epics",
            icon = null
        )

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<EpicsViewModel>()
        val epicsState by viewModel.epicsState.collectAsState()
        val selectedStatus by viewModel.selectedStatus.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        )

        LaunchedEffect(Unit) {
            viewModel.loadEpics()
        }

        val listState = rememberLazyListState()

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

                    var showJoinModal by remember { mutableStateOf(false) }

                if (showJoinModal) {
                    com.cybernerd404.fairsplit.presentation.epics.JoinEpicModal(
                        onDismiss = { showJoinModal = false },
                        onJoined = { epic ->
                            showJoinModal = false
                            navigator.push(com.cybernerd404.fairsplit.presentation.epics.detail.EpicDetailScreen(epicId = epic.id))
                        }
                    )
                }

                // Title + Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Epics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showJoinModal = true },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.AccentCyan.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Join Epic",
                                tint = FairSplitColors.AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Join",
                                fontSize = 13.sp,
                                color = FairSplitColors.AccentCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { navigator.push(CreateEpicScreen()) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Epic",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Create",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tabs: Active / Settled
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    EpicStatusTabItem(
                        title = "Active",
                        isSelected = selectedStatus == EpicStatus.ACTIVE,
                        onClick = { viewModel.setStatus(EpicStatus.ACTIVE) },
                        modifier = Modifier.weight(1f)
                    )
                    EpicStatusTabItem(
                        title = "Settled",
                        isSelected = selectedStatus == EpicStatus.SETTLED,
                        onClick = { viewModel.setStatus(EpicStatus.SETTLED) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Epics List
                when {
                    epicsState.isLoading() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    epicsState.isError() -> {
                        ErrorState(
                            message = epicsState.error?.message ?: "Failed to load epics",
                            onRetry = { viewModel.loadEpics() }
                        )
                    }
                    epicsState.isSuccess() && epicsState.data != null -> {
                        val epics = epicsState.data!!
                        if (epics.isEmpty()) {
                            EmptyState(
                                title = "No ${selectedStatus.name.lowercase()} epics",
                                message = "Create a new epic to start splitting group expenses seamlessly."
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                items(epics, key = { it.id }) { epic ->
                                    EpicFullCard(
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
private fun EpicStatusTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EpicFullCard(
    epic: Epic,
    onClick: () -> Unit
) {
    val isOwe = epic.yourBalance < -0.01
    val isGet = epic.yourBalance > 0.01
    val isSettled = !isOwe && !isGet

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
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = epic.name.uppercase(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${epic.totalSpent.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${epic.memberCount} members",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Member Avatars row preview
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    epic.members.take(4).forEach { member ->
                        com.cybernerd404.fairsplit.presentation.components.UserAvatar(
                            name = member.name,
                            avatarUrl = member.avatarUrl,
                            avatarInitials = member.avatarInitials,
                            size = 28.dp,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (epic.yourSpent > 0) "You spent ₹${epic.yourSpent.toInt()}" else "No spend yet",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(balanceColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = balanceText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                }
            }
        }
    }
}
