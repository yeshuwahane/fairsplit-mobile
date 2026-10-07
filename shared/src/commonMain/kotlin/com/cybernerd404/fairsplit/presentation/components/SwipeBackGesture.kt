package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import kotlinx.coroutines.launch
import kotlin.math.abs

val BACK_SWIPE_EDGE_WIDTH = 32.dp
val BACK_SWIPE_MIN_DISTANCE = 120.dp

/**
 * Global coordinator that tracks whether the current navigation pop was initiated
 * interactively by [SwipeBackContainer].
 *
 * When true, [FairSplitSlideTransition] avoids running a redundant second pop animation.
 */
object NavigationTransitionController {
    var isInteractivePop: Boolean by mutableStateOf(false)
}

/**
 * Screen transition container for Voyager that cooperates with [SwipeBackContainer].
 *
 * - When [NavigationTransitionController.isInteractivePop] is true on a pop:
 *   The gesture already completed its smooth slide-off animation, so Voyager switches
 *   screens instantly (EnterTransition.None + ExitTransition.None) with zero duplicate animation.
 * - When a normal pop occurs (top bar back button, system back), standard slide animation plays.
 * - When a screen is pushed, standard slide animation plays.
 */
@Composable
fun FairSplitSlideTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )
) {
    ScreenTransition(
        navigator = navigator,
        modifier = modifier,
        transition = {
            if (NavigationTransitionController.isInteractivePop && navigator.lastEvent == StackEvent.Pop) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val (initialOffset, targetOffset) = when (navigator.lastEvent) {
                    StackEvent.Pop -> ({ size: Int -> -size }) to ({ size: Int -> size })
                    else -> ({ size: Int -> size }) to ({ size: Int -> -size })
                }
                slideInHorizontally(animationSpec, initialOffset) togetherWith
                    slideOutHorizontally(animationSpec, targetOffset)
            }
        }
    )

    LaunchedEffect(navigator.lastItem) {
        if (NavigationTransitionController.isInteractivePop) {
            NavigationTransitionController.isInteractivePop = false
        }
    }
}

/**
 * Native-feeling iOS interactive swipe-back container for Compose Multiplatform.
 *
 * Requirements:
 * - Only activates when gesture starts near the left edge (<= edgeWidth, default 32.dp).
 * - Detects horizontal dominance: movement toward right, dx > dy * 1.5f.
 * - Does NOT conflict with LazyColumn, LazyRow, carousels, image zoom, or sliders.
 * - Interactive visual feedback: current screen translates with finger, backdrop dims, elevation shadow.
 * - Releases after threshold (default 120.dp or 35% width) -> completes slide off once and pops without duplicate animation.
 * - Releases before threshold -> springs back smoothly to 0.
 * - Robust cancellation: if pointer cancelled or lifted before threshold, cleanly resets to 0.
 */
@Composable
fun SwipeBackContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    edgeWidth: Dp = BACK_SWIPE_EDGE_WIDTH,
    threshold: Dp = BACK_SWIPE_MIN_DISTANCE,
    onPop: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    var isPopping by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxPx = constraints.maxWidth.toFloat().takeIf { it > 0f } ?: 1080f
        val edgePx = with(density) { edgeWidth.toPx() }
        val thresholdPx = with(density) { threshold.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled, maxPx, edgePx, thresholdPx) {
                    if (!enabled) return@pointerInput

                    awaitEachGesture {
                        // 1. Wait for touch down first. Always suspends!
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)

                        // If screen is currently popping, ignore further touches and wait for release
                        if (isPopping) {
                            do {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            } while (event.changes.any { it.pressed })
                            return@awaitEachGesture
                        }

                        val startX = down.position.x
                        val startY = down.position.y

                        // Only start if touch begins within the left edge zone
                        if (startX > edgePx) {
                            return@awaitEachGesture
                        }

                        println("BACK_GESTURE: START at x=$startX, y=$startY (edgePx=$edgePx)")

                        var isDragging = false
                        var currentDragX = 0f
                        val touchSlop = viewConfiguration.touchSlop

                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                // Pointer lifted
                                println("BACK_GESTURE: RELEASE at dx=${change.position.x - startX}")
                                break
                            }

                            val currentX = change.position.x
                            val currentY = change.position.y
                            val dx = currentX - startX
                            val dy = abs(currentY - startY)

                            if (!isDragging) {
                                // Decide whether this is a back swipe or vertical scroll / invalid gesture
                                if (dy > dx * 1.2f && dy > touchSlop) {
                                    // Vertical movement is dominant -> yield to vertical scrolling/children
                                    println("BACK_GESTURE: CANCEL - vertical dominant (dx=$dx, dy=$dy)")
                                    break
                                }

                                if (dx > touchSlop && dx > dy * 1.5f) {
                                    // Dominant rightward horizontal swipe from left edge!
                                    isDragging = true
                                    change.consume()
                                    currentDragX = (dx - touchSlop).coerceAtLeast(0f)
                                    coroutineScope.launch {
                                        offsetX.snapTo(currentDragX)
                                    }
                                }
                            } else {
                                // Currently dragging: follow finger and consume to prevent child clicks
                                change.consume()
                                currentDragX = dx.coerceAtLeast(0f)
                                coroutineScope.launch {
                                    offsetX.snapTo(currentDragX)
                                }
                            }
                        }

                        // Gesture finished or cancelled
                        if (isDragging) {
                            val shouldPop = currentDragX >= thresholdPx || currentDragX >= maxPx * 0.35f
                            println("BACK_GESTURE: EVALUATE shouldPop=$shouldPop, dragX=$currentDragX, thresholdPx=$thresholdPx, maxPx=$maxPx")
                            if (shouldPop) {
                                isPopping = true
                                println("BACK_GESTURE: COMMIT - animating to $maxPx")
                                NavigationTransitionController.isInteractivePop = true
                                coroutineScope.launch {
                                    try {
                                        offsetX.animateTo(
                                            targetValue = maxPx,
                                            animationSpec = tween(
                                                durationMillis = 180,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        println("BACK_GESTURE: POP calling onPop()")
                                        onPop()
                                    } catch (e: Exception) {
                                        println("BACK_GESTURE: Exception during pop: ${e.message}")
                                        NavigationTransitionController.isInteractivePop = false
                                        isPopping = false
                                        offsetX.snapTo(0f)
                                    }
                                }
                            } else {
                                println("BACK_GESTURE: SPRING BACK to 0")
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        } else {
                            if (offsetX.value > 0f) {
                                println("BACK_GESTURE: RESET idle offset to 0")
                                coroutineScope.launch {
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    }
                }
        ) {
            val progress = (offsetX.value / maxPx).coerceIn(0f, 1f)

            if (progress > 0.005f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (1f - progress) * 0.35f))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX.value
                        if (offsetX.value > 0f) {
                            shadowElevation = 16.dp.toPx()
                        }
                    }
            ) {
                content()
            }
        }
    }
}
