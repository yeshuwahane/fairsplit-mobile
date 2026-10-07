package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.ui.graphics.ImageBitmap

expect fun decodeImageBitmapFromBytes(bytes: ByteArray): ImageBitmap?
