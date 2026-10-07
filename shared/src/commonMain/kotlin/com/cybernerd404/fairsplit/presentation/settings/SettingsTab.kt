package com.cybernerd404.fairsplit.presentation.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.data.local.MockDataProvider
import com.cybernerd404.fairsplit.domain.model.AuthProvider
import com.cybernerd404.fairsplit.domain.model.UserProfile
import com.cybernerd404.fairsplit.presentation.auth.AuthScreen
import com.cybernerd404.fairsplit.presentation.components.AppBackground
import com.cybernerd404.fairsplit.presentation.components.UserAvatar
import com.cybernerd404.fairsplit.presentation.main.LocalRootNavigator
import com.cybernerd404.fairsplit.presentation.onboarding.LandingOnboardingScreen
import com.cybernerd404.fairsplit.domain.auth.rememberGoogleAuthLauncher
import com.cybernerd404.fairsplit.theme.FairSplitColors

object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = "Settings",
            icon = null
        )

    @Composable
    override fun Content() {
        val rootNavigator = LocalRootNavigator.current
        val viewModel = koinScreenModel<SettingsViewModel>()
        val userProfileState by viewModel.userProfile.collectAsState()
        val userPreferencesState by viewModel.userPreferences.collectAsState()
        val linkingMessage by viewModel.linkingMessage.collectAsState()

        val googleAuthLauncher = rememberGoogleAuthLauncher { credentials, errorMessage ->
            if (credentials != null) {
                viewModel.linkGoogleWithCredentials(
                    idToken = credentials.idToken,
                    email = credentials.email,
                    displayName = credentials.displayName,
                    photoUrl = credentials.photoUrl
                )
            } else if (errorMessage != null) {
                viewModel.handleGoogleLinkError(errorMessage)
            }
        }

        var showEditProfileModal by remember { mutableStateOf(false) }

        val profile = userProfileState.data ?: LocalDatabase.getUserProfile() ?: UserProfile(
            id = "usr_me",
            name = "You",
            email = "",
            phone = "",
            upiId = "",
            avatarInitials = "ME"
        )
        val preferences = userPreferencesState.data

        val hasGoogleLinked = profile.authIdentities.any { it.provider == AuthProvider.GOOGLE } || LocalDatabase.getAuthProvider() == "GOOGLE"

        if (showEditProfileModal) {
            EditProfileModal(
                profile = profile,
                onDismiss = { showEditProfileModal = false },
                onSave = { name, email, phone, upiId, avatarUrl ->
                    viewModel.updateProfile(name, email, phone, upiId, avatarUrl)
                }
            )
        }

        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Settings",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Profile Card with Edit Capability
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .clickable { showEditProfileModal = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(
                                    name = profile.name,
                                    avatarUrl = profile.avatarUrl,
                                    avatarInitials = profile.avatarInitials,
                                    size = 54.dp,
                                    fontSize = 18.sp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = profile.name.ifBlank { "Guest User" },
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (profile.email.isNotBlank()) profile.email else "No email set",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = FairSplitColors.PrimaryGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Edit",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FairSplitColors.PrimaryGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone & UPI Details row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("MOBILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (profile.phone.isNotBlank()) profile.phone else "Not set", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("UPI ID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (profile.upiId.isNotBlank()) profile.upiId else "Not set", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = FairSplitColors.PrimaryGreen)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // LINKED AUTHENTICATION IDENTITIES (Phone + Google)
                Text(
                    text = "LINKED ACCOUNTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        // Google Auth Identity Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Google Identity",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (hasGoogleLinked && profile.email.isNotBlank()) profile.email else if (hasGoogleLinked) "Connected" else "Not connected",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (hasGoogleLinked) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FairSplitColors.PrimaryGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Linked",
                                        color = FairSplitColors.PrimaryGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = { googleAuthLauncher.launch() }
                                ) {
                                    Text("Connect", color = Color(0xFF4285F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (linkingMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = linkingMessage ?: "",
                        fontSize = 12.sp,
                        color = FairSplitColors.AccentCyan,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Preferences Section
                Text(
                    text = "PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        SettingItemRow(
                            icon = Icons.Default.CurrencyRupee,
                            title = "Default Currency",
                            value = "INR (₹)"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingItemRow(
                            icon = Icons.Default.PieChart,
                            title = "Default Split",
                            value = "Equally"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingSwitchRow(
                            icon = Icons.Default.Notifications,
                            title = "Push Notifications",
                            checked = preferences?.notificationsEnabled ?: true,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
//                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        /*SettingSwitchRow(
                            icon = Icons.Default.DarkMode,
                            title = "Dark Appearance",
                            checked = preferences?.isDarkMode ?: true,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )*/
                    }
                }

                /*Spacer(modifier = Modifier.height(24.dp))

                // Payments / UPI Section
                Text(
                    text = "PAYMENTS & INTEGRATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )*/

                /*Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        *//*Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "UPI & Payment Gateway",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (profile.upiId.isNotBlank()) "Linked to ${profile.upiId}" else "Not configured",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }*//*

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FairSplitColors.PrimaryGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (profile.upiId.isNotBlank()) "Connected" else "Set UPI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FairSplitColors.PrimaryGreen
                            )
                        }
                    }
                }*/

                Spacer(modifier = Modifier.height(24.dp))

                // Account & Actions Section
                Text(
                    text = "ACCOUNT & ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    rootNavigator?.push(LandingOnboardingScreen())
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = FairSplitColors.PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Replay App Welcome Slides",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    MockDataProvider.resetAllData()
                                    rootNavigator?.replaceAll(com.cybernerd404.fairsplit.presentation.onboarding.LandingOnboardingScreen())
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = FairSplitColors.NegativeRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Clear All Local Data",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FairSplitColors.NegativeRed
                                    )
                                    Text(
                                        text = "Reset app to zero state",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = FairSplitColors.NegativeRed.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    MockDataProvider.resetAllData()
                                    rootNavigator?.replaceAll(com.cybernerd404.fairsplit.presentation.onboarding.LandingOnboardingScreen())
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = FairSplitColors.NegativeRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Log Out",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FairSplitColors.NegativeRed
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = FairSplitColors.NegativeRed.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SettingItemRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
