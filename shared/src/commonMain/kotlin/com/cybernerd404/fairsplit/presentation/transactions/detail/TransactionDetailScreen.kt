package com.cybernerd404.fairsplit.presentation.transactions.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.ErrorState
import com.cybernerd404.fairsplit.presentation.components.SwipeBackContainer
import com.cybernerd404.fairsplit.presentation.epics.detail.EpicDetailScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors
import org.koin.core.parameter.parametersOf

data class TransactionDetailScreen(val transactionId: String) : Screen {

    override val key: ScreenKey = "TransactionDetailScreen_$transactionId"

    @Composable
    override fun Content() {
        val viewModel = getScreenModel<TransactionDetailViewModel> { parametersOf(transactionId) }
        val transactionState by viewModel.transactionState.collectAsState()
        val effectiveMaxAmount by viewModel.effectiveMaxAmount.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showEditDialog by remember { mutableStateOf(false) }
        var editAmountText by remember { mutableStateOf("") }
        var editNoteText by remember { mutableStateOf("") }
        var editErrorText by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(transactionId) {
            viewModel.loadTransaction(transactionId)
        }

        SwipeBackContainer(onPop = { navigator.pop() }) {
            AppBackground {
                when {
                    transactionState.isLoading() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    transactionState.isError() -> {
                        ErrorState(
                            message = transactionState.error?.message ?: "Failed to load transaction",
                            onRetry = { viewModel.loadTransaction() }
                        )
                    }
                    transactionState.isSuccess() && transactionState.data != null -> {
                        val transaction = transactionState.data!!
                        val payer = transaction.fromMember
                        val receiver = transaction.toMember
                        val payerColor = Color(payer.avatarColorHex)
                        val receiverColor = Color(receiver.avatarColorHex)
                        val isVoided = transaction.status == com.cybernerd404.fairsplit.domain.model.TransactionStatus.VOIDED ||
                                       transaction.status == com.cybernerd404.fairsplit.domain.model.TransactionStatus.CANCELLED

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Void Settlement?") },
                                text = { Text("Are you sure you want to void this settlement? It will be cancelled and previous member balances will be restored.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteDialog = false
                                            viewModel.deleteTransaction {
                                                navigator.pop()
                                            }
                                        }
                                    ) {
                                        Text("Void Settlement", color = FairSplitColors.NegativeRed)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Keep")
                                    }
                                }
                            )
                        }

                        if (showEditDialog) {
                            AlertDialog(
                                onDismissRequest = { showEditDialog = false },
                                title = { Text("Edit Settlement Amount") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Enter the corrected settlement amount paid between ${payer.name} and ${receiver.name}.",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (effectiveMaxAmount != null) {
                                            Text(
                                                text = "Maximum outstanding debt: ₹${effectiveMaxAmount!!.toInt()}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = FairSplitColors.PrimaryGreen
                                            )
                                        }
                                        OutlinedTextField(
                                            value = editAmountText,
                                            onValueChange = {
                                                editAmountText = it
                                                editErrorText = null
                                            },
                                            label = { Text("Amount (₹)") },
                                            singleLine = true,
                                            isError = editErrorText != null,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = editNoteText,
                                            onValueChange = { editNoteText = it },
                                            label = { Text("Note / Description (Optional)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (editErrorText != null) {
                                            Text(
                                                text = editErrorText!!,
                                                fontSize = 12.sp,
                                                color = FairSplitColors.NegativeRed
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val parsed = editAmountText.toDoubleOrNull()
                                            if (parsed == null || parsed <= 0) {
                                                editErrorText = "Enter a valid positive amount. To remove, use Void."
                                                return@Button
                                            }
                                            viewModel.updateAmount(
                                                newAmount = parsed,
                                                newNote = editNoteText.ifBlank { null },
                                                onSuccess = { showEditDialog = false },
                                                onError = { editErrorText = it }
                                            )
                                        }
                                    ) {
                                        Text("Save Changes")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEditDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Top App Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { navigator.pop() },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "Settlement Details",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!isVoided) {
                                        IconButton(
                                            onClick = {
                                                editAmountText = transaction.amount.toInt().toString()
                                                editNoteText = transaction.subtitle
                                                editErrorText = null
                                                showEditDialog = true
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Settlement",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { showDeleteDialog = true },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(FairSplitColors.NegativeRed.copy(alpha = 0.15f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Void Settlement",
                                                tint = FairSplitColors.NegativeRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(40.dp))
                                    }
                                }
                            }

                            val primaryStatusColor = if (isVoided) FairSplitColors.NegativeRed else FairSplitColors.PositiveGreen

                            // Hero Amount & Direction Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.dp, primaryStatusColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(primaryStatusColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isVoided) Icons.Default.Cancel else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = primaryStatusColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isVoided) "SETTLEMENT VOIDED" else "PAYMENT SETTLED",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                color = primaryStatusColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Big Amount Display
                                    Text(
                                        text = "₹${transaction.amount.toInt()}",
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isVoided) MaterialTheme.colorScheme.onSurfaceVariant else FairSplitColors.PositiveGreen
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${transaction.date} • ${transaction.time}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        thickness = 1.dp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Payer -> Receiver Flow Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Payer Column
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            com.cybernerd404.fairsplit.presentation.components.UserAvatar(
                                                name = payer.name,
                                                avatarUrl = payer.avatarUrl,
                                                avatarInitials = payer.avatarInitials,
                                                size = 54.dp,
                                                fontSize = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (payer.isCurrentUser) "${payer.name} (You)" else payer.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Payer",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Transfer Arrow Icon
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = FairSplitColors.PositiveGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Receiver Column
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            com.cybernerd404.fairsplit.presentation.components.UserAvatar(
                                                name = receiver.name,
                                                avatarUrl = receiver.avatarUrl,
                                                avatarInitials = receiver.avatarInitials,
                                                size = 54.dp,
                                                fontSize = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (receiver.isCurrentUser) "${receiver.name} (You)" else receiver.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Receiver",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Details Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                ) {
                                    Text(
                                        text = "TRANSACTION DETAILS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Epic Info Row with Clickable Link
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Epic / Group",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (transaction.epicId.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(FairSplitColors.PrimaryGreen.copy(alpha = 0.12f))
                                                    .clickable {
                                                        navigator.push(EpicDetailScreen(epicId = transaction.epicId))
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = transaction.epicName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FairSplitColors.PrimaryGreen
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = "View Epic",
                                                    tint = FairSplitColors.PrimaryGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = transaction.epicName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Note / Description Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Description / Note",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = transaction.subtitle.ifEmpty { "Debt settlement payment" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Transaction ID Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Transaction ID",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = transaction.id,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (transaction.epicId.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        navigator.push(EpicDetailScreen(epicId = transaction.epicId))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open '${transaction.epicName}'",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Transaction not found",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
