package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.domain.model.Settlement
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs

@Composable
fun EpicMemberRelationshipGraph(
    epic: Epic,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val otherMembers = epic.members.filter { !it.isCurrentUser }
    var selectedMemberId by remember { mutableStateOf<String?>(otherMembers.firstOrNull()?.id) }

    val infiniteTransition = rememberInfiniteTransition()
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val selectedMember = otherMembers.firstOrNull { it.id == selectedMemberId }
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
                    text = "Balances & Flow Between You & Crew",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Tap member to inspect",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Members network row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // YOU Node
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
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(FairSplitColors.PrimaryGreen, Color(0xFF047857))
                            )
                        )
                        .border(2.dp, FairSplitColors.PrimaryGreenLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "YOU",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentUser.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Relationship lines
            otherMembers.forEach { member ->
                val isSelected = member.id == selectedMemberId
                val mColor = Color(member.avatarColorHex)

                // Check settlement link with this member
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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                            else Color.Transparent
                        )
                        .clickable { selectedMemberId = member.id }
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(mColor.copy(alpha = 0.2f))
                            .border(
                                width = if (isSelected) 2.dp else 1.5.dp,
                                color = if (isSelected) mColor else mColor.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
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
                        text = member.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = when {
                            oweToMember -> "-$currencySymbol${st!!.amount.toInt()}"
                            getFromMember -> "+$currencySymbol${st!!.amount.toInt()}"
                            else -> "Settled"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }

        // Expanded Relationship Detail Card
        if (selectedMember != null) {
            Spacer(modifier = Modifier.height(14.dp))

            val mColor = Color(selectedMember.avatarColorHex)
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
