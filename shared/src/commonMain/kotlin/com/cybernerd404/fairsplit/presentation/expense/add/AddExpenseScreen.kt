package com.cybernerd404.fairsplit.presentation.expense.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.AttachedImageCard
import com.cybernerd404.fairsplit.presentation.components.ImagePickerModal
import com.cybernerd404.fairsplit.presentation.components.ImageSource
import com.cybernerd404.fairsplit.presentation.components.PhotoPreviewModal
import com.cybernerd404.fairsplit.presentation.components.rememberMediaPickerLauncher
import com.cybernerd404.fairsplit.presentation.components.SwipeBackContainer
import com.cybernerd404.fairsplit.theme.FairSplitColors
import org.koin.core.parameter.parametersOf

data class AddExpenseScreen(
    val initialEpicId: String? = null,
    val existingExpenseId: String? = null
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = getScreenModel<AddExpenseViewModel> { parametersOf(initialEpicId, existingExpenseId) }
        val epics by viewModel.epics.collectAsState()
        val selectedEpic by viewModel.selectedEpic.collectAsState()
        val amountText by viewModel.amountText.collectAsState()
        val expenseName by viewModel.expenseName.collectAsState()
        val selectedPayer by viewModel.selectedPayer.collectAsState()
        val selectedParticipantIds by viewModel.selectedParticipantIds.collectAsState()
        val selectedCategory by viewModel.selectedCategory.collectAsState()
        val receiptUrl by viewModel.receiptUrl.collectAsState()
        val hasReceipt by viewModel.hasReceipt.collectAsState()
        val note by viewModel.note.collectAsState()
        val saveState by viewModel.saveState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        var showEpicDropdown by remember { mutableStateOf(false) }
        var showPayerDropdown by remember { mutableStateOf(false) }
        var showImagePicker by remember { mutableStateOf(false) }
        var showPhotoPreview by remember { mutableStateOf(false) }

        val mediaLauncher = rememberMediaPickerLauncher(
            onImagePicked = { uri ->
                viewModel.onReceiptSelected(uri)
            },
            onPermissionDenied = {
                // Ignore permission rejection
            }
        )

        if (showImagePicker) {
            ImagePickerModal(
                title = "Attach Bill or Receipt",
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

        if (showPhotoPreview && receiptUrl != null) {
            PhotoPreviewModal(
                imageUri = receiptUrl!!,
                title = "Attached Bill Preview",
                onDismiss = { showPhotoPreview = false },
                onChangePhoto = {
                    showPhotoPreview = false
                    showImagePicker = true
                },
                onRemovePhoto = {
                    viewModel.onReceiptSelected(null)
                    showPhotoPreview = false
                }
            )
        }

        val perPerson = viewModel.getPerPersonAmount()
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

        SwipeBackContainer(onPop = { navigator.pop() }) {
            AppBackground(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = if (viewModel.isEditMode) "Edit Expense" else "Add Expense",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Select Epic Picker
                if (epics.size > 1) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                .clickable { showEpicDropdown = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Epic / Group",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedEpic?.name ?: "Select Epic",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showEpicDropdown,
                            onDismissRequest = { showEpicDropdown = false }
                        ) {
                            epics.forEach { epic ->
                                DropdownMenuItem(
                                    text = { Text(epic.name) },
                                    onClick = {
                                        viewModel.onEpicSelected(epic)
                                        showEpicDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Large Amount Input Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AMOUNT",
                            fontSize = 12.sp,
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
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { viewModel.onAmountChanged(it) },
                                placeholder = {
                                    Text(
                                        "0.00",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                ),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.width(180.dp)
                            )
                        }

                        if (perPerson > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${perPerson.toInt()} per person (${selectedParticipantIds.size} people)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = FairSplitColors.PrimaryGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Expense Name
                Text(
                    text = "Expense Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = expenseName,
                    onValueChange = { viewModel.onExpenseNameChanged(it) },
                    placeholder = { Text("e.g. Dinner, Hotel, Airport Taxi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Paid By Dropdown
                Text(
                    text = "Paid By",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                            .clickable { showPayerDropdown = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedPayer?.isCurrentUser == true) "${selectedPayer?.name} (You)" else selectedPayer?.name ?: "Select Payer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showPayerDropdown,
                        onDismissRequest = { showPayerDropdown = false }
                    ) {
                        selectedEpic?.members?.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(if (member.isCurrentUser) "${member.name} (You)" else member.name) },
                                onClick = {
                                    viewModel.onPayerSelected(member)
                                    showPayerDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Split Between (Interactive Member Chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Split Between",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Select All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.selectAllParticipants() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedEpic?.members?.forEach { member ->
                        val isSelected = selectedParticipantIds.contains(member.id)
                        val mColor = Color(member.avatarColorHex)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.toggleParticipant(member.id) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.cybernerd404.fairsplit.presentation.components.UserAvatar(
                                name = member.name,
                                avatarUrl = member.avatarUrl,
                                avatarInitials = member.avatarInitials,
                                size = 22.dp,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (member.isCurrentUser) "${member.name} (You)" else member.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Category Selector
                Text(
                    text = "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        val catColor = Color(category.colorHex)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) catColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) catColor else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.onCategorySelected(category) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Receipt Attachment
                AttachedImageCard(
                    imageUri = receiptUrl,
                    placeholderText = "+ Attach Bill or Receipt",
                    attachedTitle = "Bill / Receipt Attached",
                    onAttachClick = { showImagePicker = true },
                    onPreviewClick = { showPhotoPreview = true },
                    onChangeClick = { showImagePicker = true },
                    onRemoveClick = { viewModel.onReceiptSelected(null) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Note Input
                Text(
                    text = "Note (Optional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { viewModel.onNoteChanged(it) },
                    placeholder = { Text("Any details or memo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Add / Save Expense Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.submitExpense {
                            navigator.pop()
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && !saveState.isLoading(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (saveState.isLoading()) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (viewModel.isEditMode) "Save Changes" else "Add Expense",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
}
