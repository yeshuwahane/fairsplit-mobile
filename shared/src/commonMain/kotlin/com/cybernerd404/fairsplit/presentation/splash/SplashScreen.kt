package com.cybernerd404.fairsplit.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteHealthDataSource
import com.cybernerd404.fairsplit.presentation.auth.AuthScreen
import com.cybernerd404.fairsplit.presentation.main.MainScreen
import com.cybernerd404.fairsplit.presentation.onboarding.LandingOnboardingScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val healthDataSource = koinInject<RemoteHealthDataSource>()

        // Animations
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        var startContentAnimation by remember { mutableStateOf(false) }
        val contentAlpha by animateFloatAsState(
            targetValue = if (startContentAnimation) 1f else 0f,
            animationSpec = tween(900, easing = EaseOutCubic)
        )
        val contentScale by animateFloatAsState(
            targetValue = if (startContentAnimation) 1f else 0.8f,
            animationSpec = tween(900, easing = EaseOutBack)
        )

        LaunchedEffect(Unit) {
            startContentAnimation = true
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
                healthDataSource.checkHealth()
            }
            delay(1800) // Smooth splash duration

            val profile = LocalDatabase.getUserProfile()
            when {
                !LocalDatabase.isOnboardingCompleted() -> {
                    navigator.replaceAll(LandingOnboardingScreen())
                }
                !LocalDatabase.isLoggedIn() -> {
                    navigator.replaceAll(AuthScreen())
                }
                profile == null || !profile.profileCompleted || profile.name.isBlank() || profile.name.startsWith("User ") -> {
                    navigator.replaceAll(com.cybernerd404.fairsplit.presentation.auth.ProfileSetupScreen())
                }
                else -> {
                    navigator.replaceAll(MainScreen())
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D2818),
                            Color(0xFF0A1612),
                            Color(0xFF030706)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Ambient animated background glow
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .scale(pulseScale)
                    .alpha(glowAlpha)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FairSplitColors.PrimaryGreen.copy(alpha = 0.25f),
                                FairSplitColors.AccentCyan.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(contentAlpha)
                    .scale(contentScale)
            ) {
                // Logo Container with rotating glowing orbital ring
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer rotating decorative dash ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        FairSplitColors.PrimaryGreen,
                                        Color.Transparent,
                                        FairSplitColors.AccentCyan,
                                        Color.Transparent,
                                        FairSplitColors.PrimaryGreen
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Inner Core Emblem
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF059669),
                                        Color(0xFF047857)
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallSplit,
                            contentDescription = "FairSplit Logo",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(180f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Name
                Text(
                    text = "FairSplit",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline with glowing accent
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = FairSplitColors.PrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Split smarter, stay closer",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA7F3D0),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Bottom loading state indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .alpha(contentAlpha)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FairSplitColors.PrimaryGreen)
                            .scale(pulseScale)
                    )
                    Text(
                        text = "Synchronizing ledgers...",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
