package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.domain.model.Settlement
import com.cybernerd404.fairsplit.domain.model.SettlementStatus
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun SettlementFlowVisualization(
    settlements: List<Settlement>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
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
                    text = "MONEY FLOW MAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Directed Settlement Paths",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${settlements.count { it.status != SettlementStatus.SETTLED && it.amount > 0 }} Pending",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FairSplitColors.AccentAmber
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Render each flow lane
        settlements.forEach { settlement ->
            val isSettled = settlement.status == SettlementStatus.SETTLED || settlement.amount <= 0.01

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Payer Node (Left)
                    MemberFlowAvatar(
                        member = settlement.fromMember,
                        role = "Payer"
                    )

                    // Animated Flow Channel (Middle)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val start = Offset(0f, size.height / 2f)
                            val end = Offset(size.width, size.height / 2f)

                            val pathColor = if (isSettled) FairSplitColors.SettledGrey.copy(alpha = 0.3f)
                            else FairSplitColors.AccentAmber.copy(alpha = 0.4f)

                            drawLine(
                                color = pathColor,
                                start = start,
                                end = end,
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            if (!isSettled) {
                                // Animated flowing dot
                                val particleOffset = start + (end - start) * pulsePhase
                                drawCircle(
                                    color = FairSplitColors.AccentAmber,
                                    radius = 4.dp.toPx(),
                                    center = particleOffset
                                )
                            }
                        }

                        // Amount badge chip in center of flow
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSettled) FairSplitColors.PositiveGreen.copy(alpha = 0.15f)
                                    else FairSplitColors.AccentAmber.copy(alpha = 0.18f)
                                )
                                .border(
                                    1.dp,
                                    if (isSettled) FairSplitColors.PositiveGreen.copy(alpha = 0.4f)
                                    else FairSplitColors.AccentAmber.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isSettled) {
                                Text(
                                    text = "Settled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.PositiveGreen
                                )
                            } else {
                                Text(
                                    text = "$currencySymbol${settlement.amount.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.AccentAmber
                                )
                            }
                        }
                    }

                    // Receiver Node (Right)
                    MemberFlowAvatar(
                        member = settlement.toMember,
                        role = "Receiver"
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun MemberFlowAvatar(
    member: Member,
    role: String
) {
    val mColor = Color(member.avatarColorHex)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(mColor.copy(alpha = 0.2f))
                .border(1.5.dp, mColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.avatarInitials,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = mColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (member.isCurrentUser) "${member.name} (You)" else member.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = role,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
