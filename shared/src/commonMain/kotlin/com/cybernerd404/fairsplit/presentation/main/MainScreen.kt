package com.cybernerd404.fairsplit.presentation.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.cybernerd404.fairsplit.presentation.components.FairSplitSpeedDialFab
import com.cybernerd404.fairsplit.presentation.epics.EpicsTab
import com.cybernerd404.fairsplit.presentation.epics.create.CreateEpicScreen
import com.cybernerd404.fairsplit.presentation.expense.add.AddExpenseScreen
import com.cybernerd404.fairsplit.presentation.home.HomeTab
import com.cybernerd404.fairsplit.presentation.settings.SettingsTab
import com.cybernerd404.fairsplit.presentation.transactions.TransactionsTab
import com.cybernerd404.fairsplit.theme.FairSplitColors

val LocalRootNavigator = staticCompositionLocalOf<Navigator?> { null }

class MainScreen : Screen {

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow

        CompositionLocalProvider(LocalRootNavigator provides rootNavigator) {
            TabNavigator(HomeTab) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                )
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        ) {
                            FairSplitTabItem(tab = HomeTab, icon = Icons.Default.Home, label = "Home")
                            FairSplitTabItem(tab = EpicsTab, icon = Icons.Default.Groups, label = "Epics")
                            FairSplitTabItem(tab = TransactionsTab, icon = Icons.Default.ReceiptLong, label = "Transactions")
                            FairSplitTabItem(tab = SettingsTab, icon = Icons.Default.Settings, label = "Settings")
                        }
                    },
                    floatingActionButton = {
                        FairSplitSpeedDialFab(
                            onAddExpenseClick = {
                                rootNavigator.push(AddExpenseScreen())
                            },
                            onCreateEpicClick = {
                                rootNavigator.push(CreateEpicScreen())
                            }
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        CurrentTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.FairSplitTabItem(tab: Tab, icon: ImageVector, label: String) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = FairSplitColors.PrimaryGreen,
            selectedTextColor = FairSplitColors.PrimaryGreen,
            indicatorColor = FairSplitColors.PrimaryGreen.copy(alpha = 0.15f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
