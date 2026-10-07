package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cybernerd404.fairsplit.domain.model.Expense
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
fun ReceiptViewerModal(
    expense: Expense,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit
) {
    val hasPhoto = expense.receiptUrl != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasPhoto) {
                            if (expense.isSettlement) "Payment Proof Screenshot" else "Attached Bill Photo"
                        } else {
                            if (expense.isSettlement) "Payment Details" else "Receipt Summary"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 1
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (hasPhoto) {
                    // Full High-Definition Photo Preview with Zoom & Pan Support
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            ZoomableImageView(
                                imageUri = expense.receiptUrl!!,
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = "Attached Bill",
                                enableFullScreenButton = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clean metadata summary card with proper flex spacing
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = expense.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Paid by ${expense.paidBy.name} • ${expense.date}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                Text(
                                    text = "$currencySymbol${expense.amount.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (expense.isSettlement) FairSplitColors.PositiveGreen else FairSplitColors.PrimaryGreen,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                } else if (expense.isSettlement) {
                    // Digital Payment Proof Card (only when no photo is attached)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, FairSplitColors.PositiveGreen.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(FairSplitColors.PositiveGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = FairSplitColors.PositiveGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Paid Successfully",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "₹${expense.amount.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FairSplitColors.PositiveGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                        Spacer(modifier = Modifier.height(14.dp))

                        val receiver = expense.participants.firstOrNull() ?: expense.paidBy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("To", fontSize = 12.sp, color = Color.Gray)
                            Text(receiver.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("From", fontSize = 12.sp, color = Color.Gray)
                            Text(expense.paidBy.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date & Time", fontSize = 12.sp, color = Color.Gray)
                            Text("${expense.date}, ${expense.createdAtTime}", fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                } else {
                    // Automated Paper Bill (only when NO photo is attached)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF9FAFB))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFF4B5563),
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = expense.title.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1F2937)
                        )

                        Text(
                            text = expense.epicName,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF6B7280)
                        )

                        Text(
                            text = "Date: ${expense.date} • Digital Bill",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF9CA3AF)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - - -",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFD1D5DB)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL PAID",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "$currencySymbol${expense.amount.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF10B981)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Paid by: ${expense.paidBy.name}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}
