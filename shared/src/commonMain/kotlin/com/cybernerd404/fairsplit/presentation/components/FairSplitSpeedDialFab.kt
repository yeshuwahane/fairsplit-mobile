package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun FairSplitSpeedDialFab(
    onAddExpenseClick: () -> Unit,
    onCreateEpicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Scrim when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
        ) {
            // Speed Dial Items
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { it / 2 } + scaleIn(tween(200)),
                exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { it / 2 } + scaleOut(tween(150))
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Create Epic Option
                    SpeedDialActionItem(
                        label = "Create Epic",
                        subtitle = "New group / trip",
                        icon = Icons.Default.GroupAdd,
                        iconBgColor = Color(0xFF6366F1),
                        onClick = {
                            isExpanded = false
                            onCreateEpicClick()
                        }
                    )

                    // 2. Add Expense Option
                    SpeedDialActionItem(
                        label = "Add Expense",
                        subtitle = "Split a bill",
                        icon = Icons.Default.ReceiptLong,
                        iconBgColor = FairSplitColors.PrimaryGreen,
                        onClick = {
                            isExpanded = false
                            onAddExpenseClick()
                        }
                    )
                }
            }

            // Main FAB
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                contentColor = if (isExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isExpanded) "Close actions" else "Add actions",
                    modifier = Modifier
                        .size(26.dp)
                        .rotate(rotationAngle)
                )
            }
        }
    }
}

@Composable
private fun SpeedDialActionItem(
    label: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
