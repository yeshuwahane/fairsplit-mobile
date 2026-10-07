package com.cybernerd404.fairsplit.presentation.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.theme.FairSplitColors

import com.cybernerd404.fairsplit.presentation.components.UserAvatar
import com.cybernerd404.fairsplit.presentation.components.rememberMediaPickerLauncher
import com.cybernerd404.fairsplit.presentation.components.ImageSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModal(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, phone: String, upiId: String, avatarUrl: String?) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }
    var upiId by remember { mutableStateOf(profile.upiId) }
    var avatarUrl by remember { mutableStateOf(profile.avatarUrl) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val mediaPickerLauncher = rememberMediaPickerLauncher(
        onImagePicked = { path ->
            avatarUrl = path
            showAvatarPicker = false
        },
        onPermissionDenied = { _ -> }
    )

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User Avatar with Edit Badge
                UserAvatar(
                    name = name,
                    avatarUrl = avatarUrl,
                    avatarInitials = profile.avatarInitials,
                    size = 76.dp,
                    fontSize = 26.sp,
                    showEditBadge = true,
                    onClick = { showAvatarPicker = true }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showAvatarPicker = !showAvatarPicker }) {
                        Text(
                            text = if (showAvatarPicker) "Hide Options" else if (avatarUrl != null) "Change Photo" else "Choose Avatar",
                            color = FairSplitColors.PrimaryGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (avatarUrl != null) {
                        TextButton(onClick = { avatarUrl = null }) {
                            Text(
                                text = "Remove",
                                color = FairSplitColors.NegativeRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Inline Animated Avatar and Photo Picker
                AnimatedVisibility(
                    visible = showAvatarPicker,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, FairSplitColors.PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Select Photo or Avatar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    mediaPickerLauncher.launch(ImageSource.GALLERY)
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.PrimaryGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    mediaPickerLauncher.launch(ImageSource.CAMERA)
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val sampleAvatars = listOf(
                            "avatar_traveler" to "🧳 Traveler",
                            "avatar_foodie" to "🍕 Foodie",
                            "avatar_gamer" to "🎮 Gamer",
                            "avatar_coder" to "💻 Coder",
                            "avatar_explorer" to "🚀 Explorer",
                            "avatar_artist" to "🎨 Artist",
                            "avatar_party" to "🎉 Party",
                            "avatar_star" to "⭐ Star"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            sampleAvatars.take(4).forEach { (id, label) ->
                                Button(
                                    onClick = {
                                        avatarUrl = id
                                        showAvatarPicker = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (avatarUrl == id) FairSplitColors.PrimaryGreen.copy(alpha = 0.35f) else Color(0xFF1E293B)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(label, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            sampleAvatars.drop(4).forEach { (id, label) ->
                                Button(
                                    onClick = {
                                        avatarUrl = id
                                        showAvatarPicker = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (avatarUrl == id) FairSplitColors.PrimaryGreen.copy(alpha = 0.35f) else Color(0xFF1E293B)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(label, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Display Name") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FairSplitColors.PrimaryGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = FairSplitColors.PrimaryGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mobile Number Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorMessage = null },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = FairSplitColors.PrimaryGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // UPI ID Field
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it; errorMessage = null },
                    label = { Text("UPI ID (Optional)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = FairSplitColors.PrimaryGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = FairSplitColors.NegativeRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (name.isBlank()) {
                            errorMessage = "Name cannot be empty"
                        } else {
                            onSave(name.trim(), email.trim(), phone.trim(), upiId.trim(), avatarUrl)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.PrimaryGreen)
                ) {
                    Text(
                        text = "Save Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
}
