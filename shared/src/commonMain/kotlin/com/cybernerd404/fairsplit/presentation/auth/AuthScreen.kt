package com.cybernerd404.fairsplit.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.domain.auth.rememberGoogleAuthLauncher
import com.cybernerd404.fairsplit.presentation.main.MainScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors

class AuthScreen : Screen {

    override val key: ScreenKey get() = "auth_screen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<AuthViewModel>()

        val uiState by viewModel.uiState.collectAsState()

        val googleAuthLauncher = rememberGoogleAuthLauncher { credentials, errorMessage ->
            if (credentials != null) {
                viewModel.handleGoogleSignInSuccess(
                    idToken = credentials.idToken,
                    email = credentials.email,
                    displayName = credentials.displayName,
                    photoUrl = credentials.photoUrl
                )
            } else if (errorMessage != null && !errorMessage.contains("cancelled", ignoreCase = true)) {
                viewModel.handleGoogleSignInError(errorMessage)
            }
        }

        // Routing with authenticated state
        LaunchedEffect(uiState) {
            when (val state = uiState) {
                is AuthUiState.Authenticated -> {
                    if (state.isProfileCompleted) {
                        navigator.replaceAll(MainScreen())
                    } else {
                        navigator.replaceAll(ProfileSetupScreen())
                    }
                }
                else -> {}
            }
        }

        val errorMessage = (uiState as? AuthUiState.Error)?.message

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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(36.dp))

                // App Emblem
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    FairSplitColors.PrimaryGreen,
                                    Color(0xFF059669)
                                )
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "FairSplit Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(44.dp).rotate(180f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to FairSplit",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to track, split, and settle group expenses seamlessly.",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Value Highlights Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AuthFeatureRow(
                            icon = Icons.Default.Groups,
                            title = "Shared Group Epics",
                            description = "Split trips, rent, and outings with friends and roommates."
                        )
                        AuthFeatureRow(
                            icon = Icons.Default.Savings,
                            title = "Smart Debt Minimization",
                            description = "Settle with the fewest possible transfers via UPI."
                        )
                        AuthFeatureRow(
                            icon = Icons.Default.Sync,
                            title = "Instant Real-Time Sync",
                            description = "All members see balances and updates instantly."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Error message banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = FairSplitColors.NegativeRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.NegativeRed.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = FairSplitColors.NegativeRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                // Google Sign-In Button (Primary and only authentication method)
                Button(
                    onClick = {
                        viewModel.clearError()
                        googleAuthLauncher.launch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    enabled = uiState !is AuthUiState.GoogleSigningIn
                ) {
                    if (uiState is AuthUiState.GoogleSigningIn) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Styled Google 'G' Mark
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Footer terms note
                Text(
                    text = "By continuing, you agree to FairSplit Terms & Privacy Policy.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FairSplitColors.PrimaryGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FairSplitColors.PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )
        }
    }
}
