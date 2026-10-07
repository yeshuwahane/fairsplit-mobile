package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeEpicsRelationshipGraph(
    userProfile: UserProfile,
    epics: List<Epic>,
    onEpicClick: (Epic) -> Unit,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    var selectedEpicId by remember { mutableStateOf<String?>(epics.firstOrNull()?.id) }

    // Pulsing animation for active flow lines
    val infiniteTransition = rememberInfiniteTransition()
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "YOUR EPICS NETWORK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Relationship between You & Active Groups",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(FairSplitColors.PrimaryGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${epics.size} Connected",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FairSplitColors.PrimaryGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Constellation canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas drawing connection lines from Center (YOU) to Radial Epic Nodes
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.width.coerceAtMost(size.height) / 2.2f)

                val count = epics.size.coerceAtLeast(1)
                epics.forEachIndexed { index, epic ->
                    val angle = (-90.0 + (360.0 / count) * index) * (kotlin.math.PI / 180.0)
                    val nodePos = Offset(
                        x = (center.x + radius * cos(angle)).toFloat(),
                        y = (center.y + radius * sin(angle)).toFloat()
                    )

                    val isOwe = epic.yourBalance < -0.01
                    val isGet = epic.yourBalance > 0.01
                    val isSelected = epic.id == selectedEpicId

                    val lineColor = when {
                        isOwe -> FairSplitColors.NegativeRed
                        isGet -> FairSplitColors.PositiveGreen
                        else -> FairSplitColors.SettledGrey
                    }

                    // Connection Line
                    drawLine(
                        color = if (isSelected) lineColor else lineColor.copy(alpha = 0.4f),
                        start = center,
                        end = nodePos,
                        strokeWidth = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = if (!isOwe && !isGet) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )

                    // Animated particle dot flowing along the line
                    if (isOwe || isGet) {
                        val flowOffset = if (isOwe) {
                            // Outgoing from YOU to Epic
                            center + (nodePos - center) * pulsePhase
                        } else {
                            // Incoming from Epic to YOU
                            nodePos + (center - nodePos) * pulsePhase
                        }
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = flowOffset
                        )
                    }
                }
            }

            // Center Node: YOU
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FairSplitColors.PrimaryGreen,
                                Color(0xFF047857)
                            )
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "YOU",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = userProfile.name,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Epic Nodes positioned around center
            epics.forEachIndexed { index, epic ->
                val count = epics.size.coerceAtLeast(1)
                val angleDeg = -90.0 + (360.0 / count) * index

                val isOwe = epic.yourBalance < -0.01
                val isGet = epic.yourBalance > 0.01
                val isSelected = epic.id == selectedEpicId

                val balanceBadgeColor = when {
                    isOwe -> FairSplitColors.NegativeRed
                    isGet -> FairSplitColors.PositiveGreen
                    else -> FairSplitColors.SettledGrey
                }

                // Calculate alignment offset based on position
                val alignment = when {
                    angleDeg in -135.0..-45.0 -> Alignment.TopCenter
                    angleDeg in -45.0..45.0 -> Alignment.CenterEnd
                    angleDeg in 45.0..135.0 -> Alignment.BottomCenter
                    else -> Alignment.CenterStart
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = alignment
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) balanceBadgeColor else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedEpicId = epic.id
                                onEpicClick(epic)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = epic.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = when {
                                    isOwe -> "Owe $currencySymbol${abs(epic.yourBalance).toInt()}"
                                    isGet -> "Get $currencySymbol${epic.yourBalance.toInt()}"
                                    else -> "Settled"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = balanceBadgeColor
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = "Open Epic",
                            tint = balanceBadgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
