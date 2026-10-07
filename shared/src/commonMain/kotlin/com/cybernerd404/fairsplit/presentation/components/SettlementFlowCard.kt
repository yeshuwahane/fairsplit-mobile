package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.domain.model.Settlement
import com.cybernerd404.fairsplit.domain.model.SettlementStatus
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun SettlementFlowCard(
    settlement: Settlement,
    currencySymbol: String = "₹",
    onSettleClick: ((Settlement) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSettled = settlement.status == SettlementStatus.SETTLED || settlement.amount <= 0.01

    val summaryText = when {
        settlement.fromMember.isCurrentUser -> "You owe ${settlement.toMember.name} $currencySymbol${settlement.amount.toInt()}"
        settlement.toMember.isCurrentUser -> "${settlement.fromMember.name} owes you $currencySymbol${settlement.amount.toInt()}"
        else -> "${settlement.fromMember.name} owes ${settlement.toMember.name} $currencySymbol${settlement.amount.toInt()}"
    }

    val buttonText = when {
        settlement.fromMember.isCurrentUser -> "Settle ($currencySymbol${settlement.amount.toInt()})"
        settlement.toMember.isCurrentUser -> "Record Payment Received ($currencySymbol${settlement.amount.toInt()})"
        else -> "Record Payment ($currencySymbol${settlement.amount.toInt()})"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Summary headline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = summaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    settlement.fromMember.isCurrentUser -> FairSplitColors.AccentAmber
                    settlement.toMember.isCurrentUser -> FairSplitColors.PositiveGreen
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (!isSettled) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (settlement.fromMember.isCurrentUser) FairSplitColors.AccentAmber.copy(alpha = 0.15f)
                            else FairSplitColors.PositiveGreen.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (settlement.fromMember.isCurrentUser) "You Owe" else if (settlement.toMember.isCurrentUser) "You are Owed" else "Pending",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (settlement.fromMember.isCurrentUser) FairSplitColors.AccentAmber else FairSplitColors.PositiveGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // From Member (Debtor / Payer)
            MemberNode(
                member = settlement.fromMember,
                label = if (settlement.fromMember.isCurrentUser) "You pay" else "${settlement.fromMember.name} pays"
            )

            // Center Flow with Arrow and Amount
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                if (isSettled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FairSplitColors.PositiveGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Settled",
                            tint = FairSplitColors.PositiveGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Settled",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FairSplitColors.PositiveGreen
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FairSplitColors.AccentAmber.copy(alpha = 0.15f))
                            .border(1.dp, FairSplitColors.AccentAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$currencySymbol${settlement.amount.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FairSplitColors.AccentAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Pays to",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // To Member (Creditor / Receiver)
            MemberNode(
                member = settlement.toMember,
                label = if (settlement.toMember.isCurrentUser) "To you" else "To ${settlement.toMember.name}"
            )
        }

        if (!isSettled && onSettleClick != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { onSettleClick(settlement) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (settlement.fromMember.isCurrentUser)
                        FairSplitColors.AccentAmber.copy(alpha = 0.2f)
                    else FairSplitColors.PositiveGreen.copy(alpha = 0.2f),
                    contentColor = if (settlement.fromMember.isCurrentUser)
                        FairSplitColors.AccentAmber
                    else FairSplitColors.PositiveGreen
                )
            ) {
                Text(
                    text = buttonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MemberNode(member: Member, label: String) {
    val avatarColor = Color(member.avatarColorHex)
    val displayName = if (member.isCurrentUser) "You" else member.name

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(avatarColor.copy(alpha = 0.2f))
                .border(1.5.dp, avatarColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.avatarInitials,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = avatarColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
