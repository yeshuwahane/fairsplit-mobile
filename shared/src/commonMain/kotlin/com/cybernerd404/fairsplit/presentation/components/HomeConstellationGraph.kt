package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.engine.ExpenseCalculationEngine
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI


import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*

@Composable
fun HomeConstellationGraph(
    userProfile: UserProfile,
    epics: List<Epic>,
    onEpicClick: (Epic) -> Unit,
    onCreateEpicClick: (() -> Unit)? = null,
    onJoinEpicClick: (() -> Unit)? = null,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    // If userProfile arrives before summary aggregation, calculate dynamically from epics
    val effectiveUserProfile = remember(userProfile, epics) {
        if (userProfile.youOwe == 0.0 && userProfile.othersOweYou == 0.0 && userProfile.netBalance == 0.0 && epics.isNotEmpty()) {
            ExpenseCalculationEngine.calculateUserSummary(epics, userProfile)
        } else {
            userProfile
        }
    }
    val totalSpentAcrossEpics = remember(epics) {
        epics.sumOf { it.totalSpent }
    }

    // Constellation graph displays top active epics (max 5) to guarantee a clean, non-overlapping orbital view
    val maxOrbitNodes = 5
    val displayEpics = remember(epics) {
        epics.distinctBy { if (it.inviteCode.isNotBlank()) it.inviteCode.lowercase() else it.id }.take(maxOrbitNodes)
    }
    var selectedEpicId by remember(displayEpics) { mutableStateOf<String?>(displayEpics.firstOrNull()?.id) }
    val selectedEpic = remember(selectedEpicId, epics) { epics.firstOrNull { it.id == selectedEpicId } }

    // Particle flow animation
    val infiniteTransition = rememberInfiniteTransition()
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Integrated Financial Header (Open, Non-Card typography)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NET FINANCIAL POSITION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (effectiveUserProfile.netBalance >= 0) "+$currencySymbol${effectiveUserProfile.netBalance.toInt()}" else "-$currencySymbol${abs(effectiveUserProfile.netBalance).toInt()}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (effectiveUserProfile.netBalance >= 0) FairSplitColors.PositiveGreen else FairSplitColors.NegativeRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL SPENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${totalSpentAcrossEpics.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "across ${epics.size} Epics",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-metrics row without boxing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FairSplitColors.NegativeRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "You owe $currencySymbol${effectiveUserProfile.youOwe.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FairSplitColors.PositiveGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Others owe you $currencySymbol${effectiveUserProfile.othersOweYou.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Interactive Network Canvas: YOU ↔ EPICS
        // Hand-crafted coordinate offsets for 0 to 5 nodes to guarantee zero overlap
        val count = displayEpics.size
        val epicOffsets = remember(count) {
            when (count) {
                0 -> emptyList()
                1 -> listOf(Offset(0f, -95f))
                2 -> listOf(Offset(-85f, -80f), Offset(85f, -80f))
                3 -> listOf(Offset(-115f, -60f), Offset(0f, -100f), Offset(115f, -60f))
                4 -> listOf(Offset(-125f, -40f), Offset(-45f, -95f), Offset(45f, -95f), Offset(125f, -40f))
                else -> listOf(
                    Offset(-130f, -30f),
                    Offset(-70f, -85f),
                    Offset(0f, -105f),
                    Offset(70f, -85f),
                    Offset(130f, -30f)
                )
            }
        }

        if (displayEpics.isEmpty()) {
            HomeEmptyStateHero(
                userName = userProfile.name,
                onCreateEpicClick = onCreateEpicClick,
                onJoinEpicClick = onJoinEpicClick
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
            // Orbital Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.62f)
                val radius = (size.width * 0.38f)

                // Outer orbital guide rings
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.07f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                )
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.04f),
                    radius = radius * 1.35f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Central glow around YOU
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FairSplitColors.PrimaryGreen.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = 65.dp.toPx()
                    ),
                    radius = 65.dp.toPx(),
                    center = center
                )

                displayEpics.forEachIndexed { index, epic ->
                    val offsetDp = if (index < epicOffsets.size) epicOffsets[index] else Offset.Zero
                    val nodePos = center + Offset(offsetDp.x.dp.toPx(), offsetDp.y.dp.toPx())

                    val isSelected = epic.id == selectedEpicId
                    val isOwe = epic.yourBalance < -0.01
                    val isGet = epic.yourBalance > 0.01

                    val strokeColor = when {
                        isOwe -> FairSplitColors.NegativeRed
                        isGet -> FairSplitColors.PositiveGreen
                        else -> FairSplitColors.SettledGrey
                    }

                    val alpha = if (selectedEpicId == null || isSelected) 0.85f else 0.2f
                    val strokeWidth = if (isSelected) 3.dp.toPx() else 1.8.dp.toPx()

                    // Curved connection path
                    val controlPoint = Offset(
                        x = (center.x + nodePos.x) / 2f + (if (index % 2 == 0) -12f else 12f),
                        y = (center.y + nodePos.y) / 2f - 15f
                    )

                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        quadraticTo(controlPoint.x, controlPoint.y, nodePos.x, nodePos.y)
                    }

                    drawPath(
                        path = path,
                        color = strokeColor.copy(alpha = alpha),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            pathEffect = if (!isOwe && !isGet) PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) else null
                        )
                    )

                    // Animated particle along the connection
                    if (isSelected && (isOwe || isGet)) {
                        val t = if (isOwe) pulsePhase else (1f - pulsePhase)
                        val px = (1 - t) * (1 - t) * center.x + 2 * (1 - t) * t * controlPoint.x + t * t * nodePos.x
                        val py = (1 - t) * (1 - t) * center.y + 2 * (1 - t) * t * controlPoint.y + t * t * nodePos.y

                        drawCircle(
                            color = strokeColor,
                            radius = 5.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Epic Nodes Overlaid with precise non-overlapping DP positioning
            displayEpics.forEachIndexed { index, epic ->
                val offsetDp = if (index < epicOffsets.size) epicOffsets[index] else Offset.Zero
                val isSelected = epic.id == selectedEpicId
                val isOwe = epic.yourBalance < -0.01
                val isGet = epic.yourBalance > 0.01

                val nodeAccent = when {
                    isOwe -> FairSplitColors.NegativeRed
                    isGet -> FairSplitColors.PositiveGreen
                    else -> FairSplitColors.SettledGrey
                }

                Box(
                    modifier = Modifier
                        .offset(x = offsetDp.x.dp, y = (36.dp + offsetDp.y.dp))
                        .widthIn(min = 90.dp, max = 106.dp)
                        .scale(if (isSelected) 1.05f else 0.95f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) nodeAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            selectedEpicId = epic.id
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = epic.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$currencySymbol${epic.totalSpent.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = nodeAccent
                        )
                        Text(
                            text = when {
                                isOwe -> "Owe $currencySymbol${abs(epic.yourBalance).toInt()}"
                                isGet -> "Get $currencySymbol${epic.yourBalance.toInt()}"
                                else -> "Settled"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Central Dominant Node: YOU
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = 36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectedEpicId = null
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    FairSplitColors.PrimaryGreen,
                                    Color(0xFF065F46)
                                )
                            )
                        )
                        .border(2.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YOU",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                        Text(
                            text = userProfile.avatarInitials,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userProfile.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        }

        // Active Epic Contextual Action Banner
        AnimatedVisibility(
            visible = selectedEpic != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (selectedEpic != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { onEpicClick(selectedEpic) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedEpic.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${selectedEpic.memberCount} members",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Total $currencySymbol${selectedEpic.totalSpent.toInt()} • Tap to enter Epic graph",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Enter Epic",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyStateHero(
    userName: String,
    onCreateEpicClick: (() -> Unit)?,
    onJoinEpicClick: (() -> Unit)?
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.75f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    FairSplitColors.PrimaryGreen.copy(alpha = 0.45f),
                    Color(0xFF38BDF8).copy(alpha = 0.2f),
                    Color.Transparent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Glowing Orb
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(pulse),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    FairSplitColors.PrimaryGreen.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(FairSplitColors.PrimaryGreen, Color(0xFF0D9488))
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FairSplitColors.PrimaryGreen.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.PrimaryGreen.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "✨ ALL SETTLED • ZERO BALANCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = FairSplitColors.PrimaryGreen,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Welcome${if (userName.isNotBlank()) ", $userName" else ""}!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "FairSplit simplifies group expenses, roommates' rent & bills, and trips. Create an Epic or enter an invite code to get started.",
                fontSize = 12.sp,
                color = Color.LightGray.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onCreateEpicClick != null) {
                    Button(
                        onClick = onCreateEpicClick,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.PrimaryGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Epic", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (onJoinEpicClick != null) {
                    OutlinedButton(
                        onClick = onJoinEpicClick,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.AccentCyan.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = FairSplitColors.AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Join via Link", color = FairSplitColors.AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
