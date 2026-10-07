package com.cybernerd404.fairsplit.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeImageBitmapFromBytes(bytes: ByteArray): ImageBitmap? {
    return try {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
