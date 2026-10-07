package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs

@Composable
fun PositionCard(
    youSpent: Double,
    yourBalance: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val isOwe = yourBalance < -0.01
    val isGet = yourBalance > 0.01
    val isSettled = !isOwe && !isGet

    val balanceColor = when {
        isOwe -> FairSplitColors.NegativeRed
        isGet -> FairSplitColors.PositiveGreen
        else -> FairSplitColors.SettledGrey
    }

    val balanceText = when {
        isOwe -> "You owe"
        isGet -> "You get back"
        else -> "Settled up"
    }

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "YOUR POSITION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // You spent
                Column {
                    Text(
                        text = "You spent",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currencySymbol${youSpent.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Vertical divider
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )

                // Your Balance (owe / get)
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isOwe -> Icons.Default.ArrowDownward
                                isGet -> Icons.Default.ArrowUpward
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = balanceColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = balanceText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = balanceColor
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSettled) "₹0" else "$currencySymbol${abs(yourBalance).toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                }
            }
        }
    }
}
