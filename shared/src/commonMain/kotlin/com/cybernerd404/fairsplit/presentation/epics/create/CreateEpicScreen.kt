package com.cybernerd404.fairsplit.presentation.epics.create

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.SwipeBackContainer
import com.cybernerd404.fairsplit.theme.FairSplitColors

class CreateEpicScreen : Screen {

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<CreateEpicViewModel>()
        val epicName by viewModel.epicName.collectAsState()
        val members by viewModel.members.collectAsState()
        val recentPeers by viewModel.recentPeers.collectAsState()
        val newMemberInput by viewModel.newMemberInput.collectAsState()
        val creationState by viewModel.creationState.collectAsState()
        val currentUserName = viewModel.currentUserName
        val navigator = LocalNavigator.currentOrThrow

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

                    // Header
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
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Create New Epic",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Epic Name Input
                    Text(
                        text = "Epic Name",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = epicName,
                        onValueChange = { viewModel.onEpicNameChanged(it) },
                        placeholder = { Text("e.g. Goa Trip, Flatmates, Ladakh 2026") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Current Selected Members Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Epic Members (${members.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Member Chips Flow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        members.forEach { memberName ->
                            val isMe = memberName.equals(currentUserName, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isMe) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "You",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (isMe) "$memberName (You)" else memberName,
                                    fontSize = 14.sp,
                                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (!isMe) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.removeMember(memberName) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick Add from Past Friends / Collaborators
                    if (recentPeers.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = FairSplitColors.AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Past Collaborators & Friends",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Tap to add people you've split with before",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentPeers.forEach { peer ->
                                val isAdded = members.any { it.equals(peer.name, ignoreCase = true) }
                                val borderColor by animateColorAsState(
                                    if (isAdded) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.outline
                                )
                                val bgColor by animateColorAsState(
                                    if (isAdded) FairSplitColors.PrimaryGreen.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surface
                                )

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { viewModel.togglePeer(peer.name) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = bgColor,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mini avatar initials circle
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(peer.avatarColorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = peer.avatarInitials.take(2),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = peer.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isAdded) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isAdded) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Icon(
                                            imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                            contentDescription = if (isAdded) "Added" else "Add",
                                            tint = if (isAdded) FairSplitColors.PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        // Empty friends state helper card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = FairSplitColors.AccentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Anyone who joins your Epics (or whose invite link you join) will automatically be saved as your Friend here for quick 1-tap adds!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Add new member manually
                    Text(
                        text = "Add Custom Friend",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMemberInput,
                            onValueChange = { viewModel.onNewMemberInputChanged(it) },
                            placeholder = { Text("Friend's name") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.addMember()
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                viewModel.addMember()
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Discord-style Invite info box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FairSplitColors.PrimaryGreen.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            FairSplitColors.PrimaryGreen.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = FairSplitColors.PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Discord-Style Instant Invite",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.PrimaryGreen
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "A shareable link & invite code will be generated immediately so you can invite anyone on WhatsApp, Discord, or Telegram.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (creationState.isError()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = creationState.error?.message ?: "Failed to create epic. Please check your network connection.",
                            color = FairSplitColors.NegativeRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Create Epic CTA
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.createEpic {
                                navigator.pop()
                            }
                        },
                        enabled = epicName.isNotBlank() && !creationState.isLoading(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (creationState.isLoading()) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Epic",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
