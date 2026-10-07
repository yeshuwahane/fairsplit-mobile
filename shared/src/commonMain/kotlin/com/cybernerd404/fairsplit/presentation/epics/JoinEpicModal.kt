package com.cybernerd404.fairsplit.presentation.epics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cybernerd404.fairsplit.data.utils.DataResource
import com.cybernerd404.fairsplit.domain.deeplink.DeepLinkManager
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.domain.usecase.GetEpicByInviteCodeUseCase
import com.cybernerd404.fairsplit.domain.usecase.JoinEpicByInviteCodeUseCase
import com.cybernerd404.fairsplit.presentation.components.QrScannerScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinEpicModal(
    initialCode: String? = null,
    onDismiss: () -> Unit,
    onJoined: (Epic) -> Unit
) {
    val getEpicUseCase: GetEpicByInviteCodeUseCase = koinInject()
    val joinEpicUseCase: JoinEpicByInviteCodeUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    var inputCode by remember { mutableStateOf(initialCode.orEmpty()) }
    var previewEpic by remember { mutableStateOf<Epic?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showQrScanner by remember { mutableStateOf(false) }

    // Helper: look up an epic by code
    fun lookupCode(code: String) {
        val clean = code.trim()
            .substringAfter("code=")
            .substringAfterLast("/")
            .trim()
        if (clean.isBlank()) return
        inputCode = clean
        scope.launch {
            isLoading = true
            errorMessage = null
            val res = getEpicUseCase(clean)
            isLoading = false
            if (res.isSuccess() && res.data != null) {
                previewEpic = res.data
                showQrScanner = false
            } else {
                errorMessage = "No Epic found with code '$clean'"
                showQrScanner = false
            }
        }
    }

    LaunchedEffect(initialCode) {
        if (!initialCode.isNullOrBlank()) {
            lookupCode(initialCode)
        }
    }

    // Full-screen QR scanner overlay (shown before dialog)
    if (showQrScanner) {
        QrScannerScreen(
            onCodeScanned = { scannedValue ->
                showQrScanner = false
                lookupCode(scannedValue)
            },
            onDismiss = { showQrScanner = false }
        )
        return
    }

    Dialog(
        onDismissRequest = {
            DeepLinkManager.clearPendingInviteCode()
            onDismiss()
        },
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
                    .padding(horizontal = 24.dp)
                    .border(1.dp, FairSplitColors.PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FairSplitColors.PrimaryGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "EPIC INVITE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FairSplitColors.PrimaryGreen,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            DeepLinkManager.clearPendingInviteCode()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Discord-style Epic Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(FairSplitColors.PrimaryGreen, Color(0xFF0284C7))
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (previewEpic != null) {
                    val epic = previewEpic!!
                    Text(
                        text = "You've been invited to join",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = epic.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Members & Info Capsule
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👥 ${epic.memberCount} Members",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = "•",
                                color = Color.Gray
                            )
                            Text(
                                text = "Code: ${epic.inviteCode}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FairSplitColors.AccentCyan
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = FairSplitColors.NegativeRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Join Button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val joinRes = joinEpicUseCase(epic.inviteCode)
                                isLoading = false
                                if (joinRes.isSuccess() && joinRes.data != null) {
                                    DeepLinkManager.clearPendingInviteCode()
                                    onJoined(joinRes.data!!)
                                } else {
                                    errorMessage = joinRes.error?.message ?: "Failed to join Epic"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.PrimaryGreen),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text(
                                text = "Accept Invite & Join Epic",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Input mode when opened manually
                    Text(
                        text = "Join an Epic",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Enter an invite link or code to join a group",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = {
                            inputCode = it
                            errorMessage = null
                        },
                        placeholder = { Text("e.g. FS-TRIP-1234 or paste link", color = Color.Gray, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = FairSplitColors.PrimaryGreen)
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
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            val clean = inputCode.trim()
                                .substringAfter("code=")
                                .substringAfterLast("/")
                                .trim()
                            if (clean.isBlank()) {
                                errorMessage = "Please enter an invite code"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                val res = getEpicUseCase(clean)
                                isLoading = false
                                if (res.isSuccess() && res.data != null) {
                                    previewEpic = res.data
                                } else {
                                    errorMessage = "No Epic found with code '$clean'"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.PrimaryGreen),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text(
                                text = "Find Epic",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.15f)
                        )
                        Text(
                            text = "OR",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.15f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showQrScanner = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.AccentCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            tint = FairSplitColors.AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan QR Code",
                            color = FairSplitColors.AccentCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                }
            }
        }
    }
}
}
