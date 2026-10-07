package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max
import kotlin.math.min

/**
 * Interactive Zoomable & Pannable Image Component.
 * Supports:
 * - Pinch to Zoom (1.0x to 5.0x)
 * - Two-finger or single-finger pan when zoomed in
 * - Double tap to toggle zoom (1x -> 2.5x -> 1x)
 * - On-screen quick zoom controls (+, -, Reset)
 * - Tap to open Full-Screen Lightbox Mode
 */
@Composable
fun ZoomableImageView(
    imageUri: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enableFullScreenButton: Boolean = true,
    initialContentScale: ContentScale = ContentScale.Fit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showFullScreen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1.2f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            offset = Offset.Zero
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    scale = newScale
                    if (newScale > 1f) {
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-1000f, 1000f),
                            y = (offset.y + pan.y).coerceIn(-1000f, 1000f)
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        PlatformImageView(
            imageUri = imageUri,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = initialContentScale
        )

        // Overlay controls for convenience
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Zoom indicator badge (when zoomed)
            AnimatedVisibility(
                visible = scale > 1.05f,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick reset / fullscreen controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (scale > 1.05f) {
                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Zoom",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (enableFullScreenButton) {
                    IconButton(
                        onClick = { showFullScreen = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Full Screen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Full Screen Interactive Modal
    if (showFullScreen) {
        FullScreenImageModal(
            imageUri = imageUri,
            onDismiss = { showFullScreen = false }
        )
    }
}

/**
 * Fullscreen Interactive Lightbox Modal.
 * Offers edge-to-edge dark background, pinch-to-zoom up to 6x, pan, zoom buttons, and reset.
 */
@Composable
fun FullScreenImageModal(
    imageUri: String,
    title: String = "Full Screen Viewer",
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Interactive Image Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                    offset = Offset.Zero
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 6f)
                            scale = newScale
                            if (newScale > 1f) {
                                offset = Offset(
                                    x = (offset.x + pan.x).coerceIn(-1500f, 1500f),
                                    y = (offset.y + pan.y).coerceIn(-1500f, 1500f)
                                )
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                PlatformImageView(
                    imageUri = imageUri,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Fit
                )
            }

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zoom: ${(scale * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom Toolbar Controls (+, -, 1x, fit)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zoom Out
                IconButton(
                    onClick = {
                        val newScale = max(1f, scale - 0.5f)
                        scale = newScale
                        if (newScale <= 1f) offset = Offset.Zero
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = if (scale > 1f) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Reset 1x
                TextButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    }
                ) {
                    Text(
                        text = "1x Reset",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Zoom In
                IconButton(
                    onClick = {
                        scale = min(6f, scale + 0.5f)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = if (scale < 6f) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
