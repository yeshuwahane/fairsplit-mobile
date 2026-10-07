package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import qrcode.QRCode
import qrcode.color.Colors

/**
 * Renders a QR code bitmap for [content] using the pure-KMP qrcode-kotlin library.
 */
@Composable
fun QrCodeImage(
    content: String,
    size: Dp = 200.dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    foregroundColor: Color = Color.Black
) {
    var imageBitmap by remember(content) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(content) { mutableStateOf(true) }

    LaunchedEffect(content) {
        isLoading = true
        imageBitmap = null
        val bytes = withContext(Dispatchers.Default) {
            try {
                QRCode.ofSquares()
                    .withColor(Colors.BLACK)
                    .withBackgroundColor(Colors.WHITE)
                    .withSize(10)
                    .build(content)
                    .render()
                    .getBytes()
            } catch (e: Exception) {
                null
            }
        }
        if (bytes != null) {
            imageBitmap = decodeImageBitmapFromBytes(bytes)
        }
        isLoading = false
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            imageBitmap != null -> Image(
                bitmap = imageBitmap!!,
                contentDescription = "QR Code",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }
    }
}
