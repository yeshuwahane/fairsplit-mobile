package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EpicMemberConstellationGraph(
    epic: Epic,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val otherMembers = epic.members.filter { !it.isCurrentUser }
    var selectedMemberId by remember { mutableStateOf<String?>(otherMembers.firstOrNull()?.id) }
    val selectedMember = otherMembers.firstOrNull { it.id == selectedMemberId }

    val infiniteTransition = rememberInfiniteTransition()
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val relevantSettlement = epic.settlements.firstOrNull {
        (it.fromMember.id == selectedMemberId && it.toMember.isCurrentUser) ||
        (it.fromMember.isCurrentUser && it.toMember.id == selectedMemberId)
    }

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
                    text = "MEMBER RELATIONSHIPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Balances & Flow: You ↔ Crew",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${epic.members.size} Members",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Orbital Network Canvas: YOU in center, members orbiting
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.width.coerceAtMost(size.height) / 2.3f)

                // Orbital dash circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.12f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                )

                // Central glow around YOU
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FairSplitColors.PrimaryGreen.copy(alpha = breatheAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = 55.dp.toPx()
                    ),
                    radius = 55.dp.toPx(),
                    center = center
                )

                val count = otherMembers.size.coerceAtLeast(1)
                otherMembers.forEachIndexed { index, member ->
                    val angle = (-90.0 + (360.0 / count) * index) * (kotlin.math.PI / 180.0)
                    val nodePos = Offset(
                        x = (center.x + radius * cos(angle)).toFloat(),
                        y = (center.y + radius * sin(angle)).toFloat()
                    )

                    val st = epic.settlements.firstOrNull {
                        (it.fromMember.id == member.id && it.toMember.isCurrentUser) ||
                        (it.fromMember.isCurrentUser && it.toMember.id == member.id)
                    }

                    val oweToMember = st?.fromMember?.isCurrentUser == true && st.amount > 0.01
                    val getFromMember = st?.toMember?.isCurrentUser == true && st.amount > 0.01
                    val isSelected = member.id == selectedMemberId

                    val strokeColor = when {
                        oweToMember -> FairSplitColors.NegativeRed
                        getFromMember -> FairSplitColors.PositiveGreen
                        else -> FairSplitColors.SettledGrey
                    }

                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(nodePos.x, nodePos.y)
                    }

                    drawPath(
                        path = path,
                        color = strokeColor.copy(alpha = if (isSelected) 0.9f else 0.35f),
                        style = Stroke(
                            width = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = if (!oweToMember && !getFromMember) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
                        )
                    )

                    // Particle pulse along the line
                    if (oweToMember || getFromMember) {
                        val t = if (oweToMember) pulsePhase else (1f - pulsePhase)
                        val px = center.x + (nodePos.x - center.x) * t
                        val py = center.y + (nodePos.y - center.y) * t

                        drawCircle(
                            color = strokeColor,
                            radius = if (isSelected) 4.5.dp.toPx() else 3.5.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Central YOU Node
            val currentUser = epic.members.firstOrNull { it.isCurrentUser } ?: Member(
                id = com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.id ?: "u1",
                name = com.cybernerd404.fairsplit.data.local.LocalDatabase.getUserProfile()?.name?.ifBlank { "You" } ?: "You",
                isCurrentUser = true
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(FairSplitColors.PrimaryGreen, Color(0xFF047857))
                            )
                        )
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YOU",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = currentUser.avatarInitials,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Radial Member Nodes
            val count = otherMembers.size.coerceAtLeast(1)
            otherMembers.forEachIndexed { index, member ->
                val angleDeg = -90.0 + (360.0 / count) * index
                val isSelected = member.id == selectedMemberId
                val mColor = Color(member.avatarColorHex)

                val st = epic.settlements.firstOrNull {
                    (it.fromMember.id == member.id && it.toMember.isCurrentUser) ||
                    (it.fromMember.isCurrentUser && it.toMember.id == member.id)
                }

                val oweToMember = st?.fromMember?.isCurrentUser == true && st.amount > 0.01
                val getFromMember = st?.toMember?.isCurrentUser == true && st.amount > 0.01

                val badgeColor = when {
                    oweToMember -> FairSplitColors.NegativeRed
                    getFromMember -> FairSplitColors.PositiveGreen
                    else -> FairSplitColors.SettledGrey
                }

                val align = when {
                    angleDeg in -135.0..-45.0 -> Alignment.TopCenter
                    angleDeg in -45.0..45.0 -> Alignment.CenterEnd
                    angleDeg in 45.0..135.0 -> Alignment.BottomCenter
                    else -> Alignment.CenterStart
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = align
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .scale(if (isSelected) 1.08f else 0.95f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) badgeColor else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedMemberId = member.id }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = member.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                oweToMember -> "Owe $currencySymbol${st!!.amount.toInt()}"
                                getFromMember -> "Get $currencySymbol${st!!.amount.toInt()}"
                                else -> "Settled"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
        }

        // Selected Member Detail Panel
        AnimatedVisibility(
            visible = selectedMember != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (selectedMember != null) {
                val isOwe = relevantSettlement?.fromMember?.isCurrentUser == true && relevantSettlement.amount > 0.01
                val isGet = relevantSettlement?.toMember?.isCurrentUser == true && relevantSettlement.amount > 0.01
                val isSettled = !isOwe && !isGet

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isOwe -> Icons.Default.ArrowForward
                                isGet -> Icons.Default.ArrowBack
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = when {
                                isOwe -> FairSplitColors.NegativeRed
                                isGet -> FairSplitColors.PositiveGreen
                                else -> FairSplitColors.SettledGrey
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when {
                                    isOwe -> "You owe ${selectedMember.name}"
                                    isGet -> "${selectedMember.name} owes you"
                                    else -> "Settled up with ${selectedMember.name}"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedMember.name}'s total spent: $currencySymbol${selectedMember.totalSpent.toInt()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = if (isSettled) "₹0" else "$currencySymbol${abs(relevantSettlement?.amount ?: 0.0).toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isOwe -> FairSplitColors.NegativeRed
                            isGet -> FairSplitColors.PositiveGreen
                            else -> FairSplitColors.SettledGrey
                        }
                    )
                }
            }
        }
    }
}
