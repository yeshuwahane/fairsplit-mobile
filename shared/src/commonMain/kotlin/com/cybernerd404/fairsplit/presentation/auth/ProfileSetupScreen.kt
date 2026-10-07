package com.cybernerd404.fairsplit.presentation.auth

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.presentation.components.ImageSource
import com.cybernerd404.fairsplit.presentation.components.UserAvatar
import com.cybernerd404.fairsplit.presentation.components.rememberMediaPickerLauncher
import com.cybernerd404.fairsplit.presentation.main.MainScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors

class ProfileSetupScreen : Screen {

    override val key: ScreenKey get() = "profile_setup_screen"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<ProfileSetupViewModel>()
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

        val uiState by viewModel.uiState.collectAsState()
        val displayName by viewModel.displayName.collectAsState()
        val email by viewModel.email.collectAsState()
        val phone by viewModel.phone.collectAsState()
        val upiId by viewModel.upiId.collectAsState()
        val avatarUrl by viewModel.avatarUrl.collectAsState()

        var showAvatarPicker by remember { mutableStateOf(false) }

        val mediaPickerLauncher = rememberMediaPickerLauncher(
            onImagePicked = { path ->
                viewModel.setAvatar(path)
                showAvatarPicker = false
            },
            onPermissionDenied = { _ -> }
        )

        // Navigation on successful profile completion
        LaunchedEffect(uiState) {
            if (uiState is ProfileSetupUiState.Success) {
                navigator.replaceAll(MainScreen())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF06100E),
                            Color(0xFF030706)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Header Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FairSplitColors.PrimaryGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.PrimaryGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "PROFILE SETUP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FairSplitColors.PrimaryGreen,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Complete Your Profile",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Set up your public profile name and optional avatar for your group expenses.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Large Circular Avatar Section
                UserAvatar(
                    name = displayName.ifBlank { "FS" },
                    avatarUrl = avatarUrl,
                    size = 110.dp,
                    fontSize = 38.sp,
                    showEditBadge = true,
                    onClick = { showAvatarPicker = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar action row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showAvatarPicker = true }
                    ) {
                        Text(
                            text = if (avatarUrl == null) "Choose Avatar / Photo" else "Change Avatar / Photo",
                            color = FairSplitColors.PrimaryGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (avatarUrl != null) {
                        TextButton(
                            onClick = { viewModel.removeAvatar() }
                        ) {
                            Text(
                                text = "Remove",
                                color = FairSplitColors.NegativeRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error Message banner
                if (uiState is ProfileSetupUiState.Error) {
                    val error = (uiState as ProfileSetupUiState.Error).message
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = FairSplitColors.NegativeRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.NegativeRed.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = error,
                            color = FairSplitColors.NegativeRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                // Input Fields Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Display Name (Required)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DISPLAY NAME",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.PrimaryGreen,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = " *",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.NegativeRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { viewModel.updateDisplayName(it) },
                                placeholder = { Text("e.g. Alex Morgan", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = FairSplitColors.PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                                )
                            )
                        }

                        // Email Address (Optional / Prefilled from Google)
                        Column {
                            Text(
                                text = "EMAIL ADDRESS (OPTIONAL)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { viewModel.updateEmail(it) },
                                placeholder = { Text("e.g. alex@example.com", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                                )
                            )
                        }

                        // Phone Number (Optional / Prefilled from Phone Auth)
                        Column {
                            Text(
                                text = "PHONE NUMBER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { viewModel.updatePhone(it) },
                                placeholder = { Text("+91 98765 00000", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                                )
                            )
                        }

                        // UPI ID (Optional)
                        Column {
                            Text(
                                text = "UPI ID (FOR RECEIVING SETTLEMENTS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { viewModel.updateUpiId(it) },
                                placeholder = { Text("e.g. username@upi", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save & Continue Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.saveProfile()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.PrimaryGreen),
                    enabled = uiState !is ProfileSetupUiState.Saving
                ) {
                    if (uiState is ProfileSetupUiState.Saving) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Save & Continue to Home",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Quick Avatar Picker Modal Sheet
            if (showAvatarPicker) {
                ModalBottomSheet(
                    onDismissRequest = { showAvatarPicker = false },
                    containerColor = Color(0xFF1E293B),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choose Profile Picture",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gallery & Camera options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    showAvatarPicker = false
                                    mediaPickerLauncher.launch(ImageSource.GALLERY)
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gallery", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    showAvatarPicker = false
                                    mediaPickerLauncher.launch(ImageSource.CAMERA)
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Camera", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF334155))
                            Text(
                                text = "  OR SELECT AN AVATAR  ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF334155))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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
                                        viewModel.setAvatar(id)
                                        showAvatarPicker = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(label, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            sampleAvatars.drop(4).forEach { (id, label) ->
                                Button(
                                    onClick = {
                                        viewModel.setAvatar(id)
                                        showAvatarPicker = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(label, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
