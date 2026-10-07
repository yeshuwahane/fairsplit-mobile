package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.model.Member
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.abs

@Composable
fun SettleUpModal(
    epic: Epic,
    preselectedDebtor: Member? = null,
    preselectedCreditor: Member? = null,
    initialAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirmSettlement: (payer: Member, receiver: Member, amount: Double, note: String?, receiptUrl: String?) -> Unit
) {
    val members = epic.members
    var selectedPayer by remember {
        mutableStateOf(
            preselectedDebtor
                ?: members.minByOrNull { it.balance }
                ?: members.firstOrNull { it.isCurrentUser }
                ?: members.first()
        )
    }

    var selectedReceiver by remember {
        mutableStateOf(
            preselectedCreditor
                ?: members.maxByOrNull { it.balance }
                ?: members.firstOrNull { it.id != selectedPayer.id }
                ?: members.last()
        )
    }

    val defaultAmount = initialAmount ?: run {
        // Calculate settlement recommendation between selected payer and receiver
        val debt = epic.settlements.firstOrNull {
            it.fromMember.id == selectedPayer.id && it.toMember.id == selectedReceiver.id
        }?.amount ?: abs(selectedPayer.balance).coerceAtLeast(0.0)
        if (debt > 0) debt else 100.0
    }

    var amountText by remember { mutableStateOf(if (defaultAmount % 1.0 == 0.0) defaultAmount.toInt().toString() else defaultAmount.toString()) }
    var selectedMethod by remember { mutableStateOf("UPI") }
    var noteText by remember { mutableStateOf("") }
    var attachedReceiptUrl by remember { mutableStateOf<String?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showPhotoPreview by remember { mutableStateOf(false) }
    val paymentMethods = listOf("UPI", "Cash")

    val mediaLauncher = rememberMediaPickerLauncher(
        onImagePicked = { uri ->
            attachedReceiptUrl = uri
        },
        onPermissionDenied = {
            // Ignore rejection
        }
    )

    if (showImagePicker) {
        ImagePickerModal(
            title = "Attach Payment Screenshot",
            onDismiss = { showImagePicker = false },
            onSelectCamera = {
                showImagePicker = false
                mediaLauncher.launch(ImageSource.CAMERA)
            },
            onSelectGallery = {
                showImagePicker = false
                mediaLauncher.launch(ImageSource.GALLERY)
            }
        )
    }

    if (showPhotoPreview && attachedReceiptUrl != null) {
        PhotoPreviewModal(
            imageUri = attachedReceiptUrl!!,
            title = "Payment Proof Preview",
            onDismiss = { showPhotoPreview = false },
            onChangePhoto = {
                showPhotoPreview = false
                showImagePicker = true
            },
            onRemovePhoto = {
                attachedReceiptUrl = null
                showPhotoPreview = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
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
                            text = "Settle Up Debt",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Payer → Receiver Visual Flow Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Payer Column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Payer",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(selectedPayer.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedPayer.avatarInitials,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedPayer.isCurrentUser) "${selectedPayer.name} (You)" else selectedPayer.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Flow Arrow Icon
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Pays to",
                                    tint = FairSplitColors.AccentAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "pays",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.AccentAmber
                                )
                            }

                            // Receiver Column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Receiver",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(selectedReceiver.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedReceiver.avatarInitials,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedReceiver.isCurrentUser) "${selectedReceiver.name} (You)" else selectedReceiver.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Amount Input
                    Text(
                        text = "SETTLEMENT AMOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = FairSplitColors.PositiveGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.width(160.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payment Method Chips (UPI & Cash only)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            val isSelected = selectedMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) FairSplitColors.PrimaryGreen.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMethod = method }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optional Note
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = {
                            Text(
                                if (selectedMethod == "UPI") "Note (e.g. Paid via Google Pay / PhonePe)" else "Note (e.g. Handed cash in person)",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payment Screenshot / Receipt Attachment
                    AttachedImageCard(
                        imageUri = attachedReceiptUrl,
                        placeholderText = "+ Attach Payment Screenshot",
                        attachedTitle = "Screenshot Attached",
                        onAttachClick = { showImagePicker = true },
                        onPreviewClick = { showPhotoPreview = true },
                        onChangeClick = { showImagePicker = true },
                        onRemoveClick = { attachedReceiptUrl = null }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Confirm CTA Button
                    val amountVal = amountText.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            if (amountVal > 0) {
                                val fullNote = if (noteText.isNotBlank()) "$selectedMethod: $noteText" else "Paid via $selectedMethod"
                                onConfirmSettlement(selectedPayer, selectedReceiver, amountVal, fullNote, attachedReceiptUrl)
                                onDismiss()
                            }
                        },
                        enabled = amountVal > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FairSplitColors.PositiveGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Record Payment (₹${amountVal.toInt()})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
