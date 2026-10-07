package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class)
actual fun decodeImageBitmapFromBytes(bytes: ByteArray): ImageBitmap? {
    return try {
        val nsData = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        val skiaImage = Image.makeFromEncoded(bytes)
        skiaImage.toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
