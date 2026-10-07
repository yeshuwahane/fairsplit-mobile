package com.cybernerd404.fairsplit.presentation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cybernerd404.fairsplit.data.local.LocalDatabase
import com.cybernerd404.fairsplit.presentation.auth.AuthScreen
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.PI


class LandingOnboardingScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val pagerState = rememberPagerState(pageCount = { 2 })
        val coroutineScope = rememberCoroutineScope()

        val onFinishOnboarding = {
            LocalDatabase.setOnboardingCompleted(true)
            navigator.replaceAll(AuthScreen())
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Header with Skip Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Mini Brand
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(FairSplitColors.PrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp).rotate(180f)
                            )
                        }
                        Text(
                            text = "FairSplit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }

                    // Skip action
                    TextButton(
                        onClick = { onFinishOnboarding() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Horizontal Pager for the 2 Slides
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    if (page == 0) {
                        OnboardingSlide1()
                    } else {
                        OnboardingSlide2()
                    }
                }

                // Bottom Controls: Page Indicator & Action Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dot Page Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(2) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 32.dp else 8.dp,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                            val color by animateColorAsState(
                                targetValue = if (isSelected) FairSplitColors.PrimaryGreen else Color.White.copy(alpha = 0.25f)
                            )

                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Next / Get Started Action Button
                    Button(
                        onClick = {
                            if (pagerState.currentPage == 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            } else {
                                onFinishOnboarding()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FairSplitColors.PrimaryGreen
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (pagerState.currentPage == 0) "Continue" else "Get Started",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide 1: Group Expense Splitting with Floating Interactive Cards
 */
@Composable
private fun OnboardingSlide1() {
    val infiniteTransition = rememberInfiniteTransition()
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual Graphic Box with floating expense cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient glow
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FairSplitColors.PrimaryGreen.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Floating Card 1: Goa Weekend Trip
            FloatingCard(
                icon = Icons.Default.Flight,
                title = "Goa Trip",
                subtitle = "Split 4 ways • ₹3,700/each",
                amount = "₹14,800",
                accentColor = FairSplitColors.PrimaryGreen,
                offsetY = (sin(floatAnim.toDouble() * (PI / 180.0)) * 8).dp,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Floating Card 2: Dinner & Drinks
            FloatingCard(
                icon = Icons.Default.Restaurant,
                title = "Friday Dinner",
                subtitle = "Paid by Sam • You owe ₹600",
                amount = "₹2,400",
                accentColor = Color(0xFFF59E0B),
                offsetY = (sin((floatAnim + 120).toDouble() * (PI / 180.0)) * 8).dp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = 20.dp)
            )

            // Floating Card 3: Apartment Rent
            FloatingCard(
                icon = Icons.Default.Home,
                title = "Flat Rent & WiFi",
                subtitle = "All 3 settled up",
                amount = "₹27,000",
                accentColor = FairSplitColors.AccentCyan,
                offsetY = (sin((floatAnim + 240).toDouble() * (PI / 180.0)) * 8).dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = (-10).dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FairSplitColors.PrimaryGreen.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.PrimaryGreen.copy(alpha = 0.3f))
        ) {
            Text(
                text = "✨ ZERO AWKWARD REMINDERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FairSplitColors.PrimaryGreen,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Headline
        Text(
            text = "Effortless Group Splitting",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle / Problem & Solution
        Text(
            text = "Track shared trips, flat rents, dinners, and group outings in real-time. Transparent balances keep friendships stress-free.",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

/**
 * Slide 2: Smart Debt Simplification & Graph Settlements
 */
@Composable
private fun OnboardingSlide2() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual Constellation & Simplification Graph Graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Graph Glow
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FairSplitColors.AccentCyan.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Center Graph Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, FairSplitColors.PrimaryGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SMART SETTLEMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FairSplitColors.PrimaryGreen,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FairSplitColors.PositiveGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "75% LESS TRANSFERS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FairSplitColors.PositiveGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simplified flow preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GraphNode("You", FairSplitColors.PrimaryGreen)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UPI ₹850", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = FairSplitColors.PositiveGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        GraphNode("Sam", Color(0xFF38BDF8))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature highlights row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = FairSplitColors.PrimaryGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Instant UPI", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = FairSplitColors.AccentCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bill Proof Photos", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FairSplitColors.AccentCyan.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, FairSplitColors.AccentCyan.copy(alpha = 0.3f))
        ) {
            Text(
                text = "⚡ GRAPH ALGORITHM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FairSplitColors.AccentCyan,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Headline
        Text(
            text = "Smart Debt Minimization",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle
        Text(
            text = "Why make 15 tangled payments when 2 can settle the entire group? Settle instantly with UPI and attach crystal-clear bill photos.",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun GraphNode(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun FloatingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    amount: String,
    accentColor: Color,
    offsetY: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .offset(y = offsetY)
            .width(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
