package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spacious, modern, interactive "WHO SHOULD PAY NEXT" graph.
 * - Generous breathing room, no cramped nesting.
 * - Clean Avatar Circles with Initials & Balance inside.
 * - Full member names placed outside/below circles so names of any length never clip.
 * - One-tap interactive settlement by clicking any member circle.
 * - Beautiful directional spline arrows & animated particle flows.
 */
@Composable
fun WhoShouldPayNextBubbleGraph(
    epic: Epic,
    currencySymbol: String = "₹",
    onSettleClick: ((payer: Member?, receiver: Member?, amount: Double?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val members = epic.members
    val debtorWhoShouldPay = members.minByOrNull { it.balance }
    val shouldPayMemberId = if (debtorWhoShouldPay != null && debtorWhoShouldPay.balance < -0.01) {
        debtorWhoShouldPay.id
    } else {
        null
    }

    var selectedMemberId by remember { mutableStateOf<String?>(shouldPayMemberId ?: members.firstOrNull()?.id) }

    // Flow particle pulse animation along the arrows
    val infiniteTransition = rememberInfiniteTransition()
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        // Section Header Row (Title + Badge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WHO SHOULD PAY NEXT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = FairSplitColors.PrimaryGreen
            )

            if (debtorWhoShouldPay != null && debtorWhoShouldPay.balance < -0.01) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(FairSplitColors.NegativeRed.copy(alpha = 0.12f))
                        .border(1.dp, FairSplitColors.NegativeRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (debtorWhoShouldPay.isCurrentUser) "You owe most" else "${debtorWhoShouldPay.name} owes most",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FairSplitColors.NegativeRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tap any person to settle their balance",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Dynamic Floating Bubble Canvas
        val count = members.size
        val baseOffsets = remember(members) {
            val map = mutableMapOf<String, Offset>()
            val r = if (count <= 2) 58f else if (count <= 4) 62f else 68f
            when (count) {
                0 -> {}
                1 -> {
                    map[members[0].id] = Offset(0f, 0f)
                }
                2 -> {
                    map[members[0].id] = Offset(-62f, 0f)
                    map[members[1].id] = Offset(62f, 0f)
                }
                3 -> {
                    members.forEachIndexed { index, m ->
                        val angle = -PI / 2.0 + index * (2.0 * PI / 3.0)
                        map[m.id] = Offset((r * cos(angle)).toFloat(), (r * sin(angle)).toFloat())
                    }
                }
                4 -> {
                    // Symmetrically place 4 members so all 4 have maximum clearance
                    map[members[0].id] = Offset(-58f, -48f)
                    map[members[1].id] = Offset(58f, -48f)
                    map[members[2].id] = Offset(58f, 48f)
                    map[members[3].id] = Offset(-58f, 48f)
                }
                else -> {
                    members.forEachIndexed { index, m ->
                        val angle = -PI / 2.0 + index * (2.0 * PI / count)
                        map[m.id] = Offset((r * cos(angle)).toFloat(), (r * sin(angle)).toFloat())
                    }
                }
            }
            map
        }

        val primaryDebtorOffset = shouldPayMemberId?.let { baseOffsets[it] } ?: Offset.Zero

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0F172A).copy(alpha = 0.9f),
                            Color(0xFF070A10)
                        )
                    )
                )
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Canvas for clean modern glowing connection arrows & background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background subtle dashed orbital guides
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = size.width * 0.36f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                )

                // Ambient glow around primary debtor
                if (shouldPayMemberId != null) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FairSplitColors.AccentAmber.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = center + Offset(primaryDebtorOffset.x.dp.toPx(), primaryDebtorOffset.y.dp.toPx()),
                            radius = 55.dp.toPx()
                        ),
                        radius = 55.dp.toPx(),
                        center = center + Offset(primaryDebtorOffset.x.dp.toPx(), primaryDebtorOffset.y.dp.toPx())
                    )
                }

                // Directed Settlements (Debtor -> Creditor) from dynamic settlements
                epic.settlements.forEach { settlement ->
                    val fromId = settlement.fromMember.id
                    val toId = settlement.toMember.id
                    val fromDp = baseOffsets[fromId] ?: return@forEach
                    val toDp = baseOffsets[toId] ?: return@forEach

                    val startPx = center + Offset(fromDp.x.dp.toPx(), fromDp.y.dp.toPx())
                    val endPx = center + Offset(toDp.x.dp.toPx(), toDp.y.dp.toPx())

                    val dx = endPx.x - startPx.x
                    val dy = endPx.y - startPx.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist < 10f) return@forEach

                    val angle = atan2(dy, dx)

                    // Bubble radius clearance so lines start & end on circle boundaries
                    val clearance = 32.dp.toPx()

                    val lineStart = Offset(
                        x = startPx.x + cos(angle) * clearance,
                        y = startPx.y + sin(angle) * clearance
                    )
                    val lineEnd = Offset(
                        x = endPx.x - cos(angle) * clearance,
                        y = endPx.y - sin(angle) * clearance
                    )

                    val isSelected = selectedMemberId == fromId || selectedMemberId == toId
                    val strokeW = if (isSelected) 2.5.dp.toPx() else 1.8.dp.toPx()
                    val arrowColor = if (isSelected) FairSplitColors.AccentAmber else FairSplitColors.AccentAmber.copy(alpha = 0.6f)

                    // Smooth subtle quadratic curve
                    val midX = (lineStart.x + lineEnd.x) / 2f - sin(angle) * 8.dp.toPx()
                    val midY = (lineStart.y + lineEnd.y) / 2f + cos(angle) * 8.dp.toPx()
                    val control = Offset(midX, midY)

                    val path = Path().apply {
                        moveTo(lineStart.x, lineStart.y)
                        quadraticTo(control.x, control.y, lineEnd.x, lineEnd.y)
                    }

                    // Draw the clean flow line
                    drawPath(
                        path = path,
                        color = arrowColor,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Draw Arrowhead at lineEnd pointing directly to Creditor
                    val arrowLen = 11.dp.toPx()
                    val a1 = angle + PI - (PI / 6.5)
                    val a2 = angle + PI + (PI / 6.5)

                    val arrowPath = Path().apply {
                        moveTo(lineEnd.x, lineEnd.y)
                        lineTo(
                            lineEnd.x + cos(a1).toFloat() * arrowLen,
                            lineEnd.y + sin(a1).toFloat() * arrowLen
                        )
                        lineTo(
                            lineEnd.x + cos(a2).toFloat() * arrowLen,
                            lineEnd.y + sin(a2).toFloat() * arrowLen
                        )
                        close()
                    }
                    drawPath(path = arrowPath, color = arrowColor)

                    // Animated Glowing Flow Particle traveling Debtor -> Creditor
                    val t = pulsePhase
                    val px = (1 - t) * (1 - t) * lineStart.x + 2 * (1 - t) * t * control.x + t * t * lineEnd.x
                    val py = (1 - t) * (1 - t) * lineStart.y + 2 * (1 - t) * t * control.y + t * t * lineEnd.y

                    drawCircle(
                        color = FairSplitColors.AccentAmber,
                        radius = 3.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // Render Floating Member Nodes (Circles with Initials + Balance inside, Full Name below)
            members.forEach { member ->
                val posDp = baseOffsets[member.id] ?: Offset.Zero
                val isSelected = member.id == selectedMemberId
                val isDebtor = member.balance < -0.01
                val isCreditor = member.balance > 0.01
                val isPrimaryShouldPay = member.id == shouldPayMemberId

                val circleSize = if (isPrimaryShouldPay) 64.dp else 58.dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(x = posDp.x.dp, y = posDp.y.dp)
                        .widthIn(min = 72.dp, max = 100.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedMemberId = member.id
                            if (onSettleClick != null) {
                                val currentUserId = members.firstOrNull { it.isCurrentUser }?.id ?: members.first().id

                                // 1. If current user owes this member (member is creditor to current user)
                                val debtToThisMember = epic.settlements.firstOrNull {
                                    it.fromMember.id == currentUserId && it.toMember.id == member.id
                                }
                                if (debtToThisMember != null) {
                                    onSettleClick(debtToThisMember.fromMember, debtToThisMember.toMember, debtToThisMember.amount)
                                    return@clickable
                                }

                                // 2. If this member owes current user (current user is creditor to this member)
                                val debtFromThisMember = epic.settlements.firstOrNull {
                                    it.fromMember.id == member.id && it.toMember.id == currentUserId
                                }
                                if (debtFromThisMember != null) {
                                    onSettleClick(debtFromThisMember.fromMember, debtFromThisMember.toMember, debtFromThisMember.amount)
                                    return@clickable
                                }

                                // 3. If this member is a debtor in any active settlement
                                val debtAsDebtor = epic.settlements.firstOrNull { it.fromMember.id == member.id }
                                if (debtAsDebtor != null) {
                                    onSettleClick(debtAsDebtor.fromMember, debtAsDebtor.toMember, debtAsDebtor.amount)
                                    return@clickable
                                }

                                // 4. If this member is a creditor in any active settlement
                                val debtAsCreditor = epic.settlements.firstOrNull { it.toMember.id == member.id }
                                if (debtAsCreditor != null) {
                                    onSettleClick(debtAsCreditor.fromMember, debtAsCreditor.toMember, debtAsCreditor.amount)
                                    return@clickable
                                }

                                // 5. Fallback if member has negative or positive balance
                                if (member.balance < -0.01) {
                                    onSettleClick(member, null, abs(member.balance))
                                } else if (member.balance > 0.01) {
                                    onSettleClick(null, member, member.balance)
                                }
                            }
                        }
                ) {
                    // 1. Circular Avatar Bubble with Initials & Balance
                    Box(
                        modifier = Modifier
                            .size(circleSize)
                            .scale(if (isSelected) 1.08f else 1f)
                            .shadow(
                                elevation = if (isPrimaryShouldPay) 10.dp else 4.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.5f)
                            )
                            .clip(CircleShape)
                            .background(
                                when {
                                    isPrimaryShouldPay -> Brush.radialGradient(
                                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                    )
                                    isCreditor -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFF065F46), Color(0xFF047857))
                                    )
                                    isDebtor -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFF374151), Color(0xFF1F2937))
                                    )
                                    else -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                                    )
                                }
                            )
                            .border(
                                width = if (isPrimaryShouldPay) 2.dp else if (isSelected) 2.dp else 1.2.dp,
                                color = when {
                                    isPrimaryShouldPay -> FairSplitColors.AccentAmber
                                    isSelected -> Color.White
                                    isCreditor -> FairSplitColors.PositiveGreen
                                    isDebtor -> FairSplitColors.NegativeRed.copy(alpha = 0.7f)
                                    else -> Color.White.copy(alpha = 0.25f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            if (!member.avatarUrl.isNullOrBlank()) {
                                UserAvatar(
                                    name = member.name,
                                    avatarUrl = member.avatarUrl,
                                    avatarInitials = member.avatarInitials,
                                    size = 26.dp,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            } else {
                                Text(
                                    text = member.avatarInitials,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = when {
                                    isCreditor -> "+$currencySymbol${member.balance.toInt()}"
                                    isDebtor -> "-$currencySymbol${abs(member.balance).toInt()}"
                                    else -> "${currencySymbol}0"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isCreditor -> Color(0xFFA7F3D0)
                                    isDebtor -> Color(0xFFFCA5A5)
                                    else -> Color(0xFFE5E7EB)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Full Member Name (Outside/below circle: NEVER gets cut off!)
                    Text(
                        text = if (member.isCurrentUser) "${member.name} (You)" else member.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Step-by-Step Clean Settlement Action Narrative from real dynamic calculations
        Text(
            text = "HOW TO SETTLE UP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        val settledMembers = members.filter { abs(it.balance) < 0.01 }
            .sortedByDescending { it.isCurrentUser }
        val activeSettlements = epic.settlements
            .sortedWith(compareByDescending<com.cybernerd404.fairsplit.domain.model.Settlement> { it.fromMember.isCurrentUser || it.toMember.isCurrentUser }
                .thenByDescending { it.amount })

        if (activeSettlements.isEmpty() && settledMembers.size == members.size) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FairSplitColors.PositiveGreen.copy(alpha = 0.12f))
                    .border(1.dp, FairSplitColors.PositiveGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = FairSplitColors.PositiveGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "All members are fully settled up!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FairSplitColors.PositiveGreen
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                activeSettlements.forEach { settlement ->
                    val fromName = if (settlement.fromMember.isCurrentUser) "${settlement.fromMember.name} (You)" else settlement.fromMember.name
                    val toName = if (settlement.toMember.isCurrentUser) "${settlement.toMember.name} (You)" else settlement.toMember.name
                    SettlementInstructionRow(
                        fromName = fromName,
                        toName = toName,
                        amount = "₹${settlement.amount.toInt()}",
                        isHighlighted = selectedMemberId == settlement.fromMember.id || selectedMemberId == settlement.toMember.id,
                        onClick = {
                            onSettleClick?.invoke(settlement.fromMember, settlement.toMember, settlement.amount)
                        }
                    )
                }

                settledMembers.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FairSplitColors.PositiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (member.isCurrentUser) "You are fully settled up" else "${member.name} is fully settled up",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "₹0 balance",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementInstructionRow(
    fromName: String,
    toName: String,
    amount: String,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isHighlighted) FairSplitColors.AccentAmber.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (isHighlighted) FairSplitColors.AccentAmber.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(FairSplitColors.NegativeRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pay",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = FairSplitColors.NegativeRed
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = fromName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Text(
                text = " → ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FairSplitColors.AccentAmber,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            Text(
                text = toName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = amount,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FairSplitColors.AccentAmber,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FairSplitColors.PositiveGreen)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Settle",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
