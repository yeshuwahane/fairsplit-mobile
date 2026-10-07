package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.EpicStatus
import com.cybernerd404.fairsplit.domain.model.TransactionStatus
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun StatusBadge(
    status: TransactionStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        TransactionStatus.COMPLETED -> Triple(
            FairSplitColors.PositiveGreen.copy(alpha = 0.15f),
            FairSplitColors.PositiveGreen,
            "Completed"
        )
        TransactionStatus.PENDING -> Triple(
            FairSplitColors.AccentAmber.copy(alpha = 0.15f),
            FairSplitColors.AccentAmber,
            "Pending"
        )
        TransactionStatus.FAILED -> Triple(
            FairSplitColors.NegativeRed.copy(alpha = 0.15f),
            FairSplitColors.NegativeRed,
            "Failed"
        )
        TransactionStatus.VOIDED,
        TransactionStatus.CANCELLED -> Triple(
            FairSplitColors.SettledGrey.copy(alpha = 0.25f),
            FairSplitColors.SettledGrey,
            "Voided"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun EpicStatusBadge(
    status: EpicStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        EpicStatus.ACTIVE -> Triple(
            FairSplitColors.PrimaryGreen.copy(alpha = 0.15f),
            FairSplitColors.PrimaryGreen,
            "Active"
        )
        EpicStatus.SETTLED -> Triple(
            FairSplitColors.SettledGrey.copy(alpha = 0.15f),
            FairSplitColors.SettledGrey,
            "Settled"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
