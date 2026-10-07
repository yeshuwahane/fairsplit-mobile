package com.cybernerd404.fairsplit.presentation.epics.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cybernerd404.fairsplit.domain.model.Epic
import com.cybernerd404.fairsplit.presentation.components.QrCodeImage
import com.cybernerd404.fairsplit.presentation.components.ShareLauncher
import com.cybernerd404.fairsplit.presentation.components.rememberShareLauncher
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EpicInviteSheet(
    epic: Epic,
    onDismiss: () -> Unit
) {
    val inviteCode = epic.inviteCode.ifBlank {
        "FS-${epic.name.filter { it.isLetter() }.take(4).uppercase().ifEmpty { "EPIC" }}-${epic.id.takeLast(4)}"
    }
    val inviteUrl = "https://yeshuwahane.github.io/fairsplit/join?code=$inviteCode"
    val shareMessage = "🎉 You're invited to join '${epic.name}' on FairSplit!\n\nTap to join directly:\n$inviteUrl\n\nOr open FairSplit and enter code: $inviteCode"

    val shareLauncher = rememberShareLauncher()
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FairSplitColors.AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FairSplitColors.AccentCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "INVITE TO EPIC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FairSplitColors.AccentCyan,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Epic name
                    Text(
                        text = epic.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${epic.memberCount} member${if (epic.memberCount != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Code card
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        QrCodeImage(
                            content = inviteUrl,
                            size = 200.dp,
                            backgroundColor = Color.White,
                            foregroundColor = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Invite code chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Invite Code",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = inviteCode,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.AccentCyan,
                                    letterSpacing = 1.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(inviteCode))
                                    copied = true
                                    scope.launch {
                                        delay(2000)
                                        copied = false
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (copied) FairSplitColors.PrimaryGreen.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.08f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = if (copied) FairSplitColors.PrimaryGreen else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (copied) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Code copied!",
                            fontSize = 12.sp,
                            color = FairSplitColors.PrimaryGreen,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Share link button
                    Button(
                        onClick = { shareLauncher.share(shareMessage, "Invite to ${epic.name}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairSplitColors.AccentCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Invite Link",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scan to join hint
                    Text(
                        text = "Scan the QR code or share the link to invite others",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
