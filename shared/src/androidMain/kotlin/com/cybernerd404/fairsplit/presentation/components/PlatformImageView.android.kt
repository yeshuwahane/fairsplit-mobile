package com.cybernerd404.fairsplit.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import coil3.compose.AsyncImage

@Composable
actual fun PlatformImageView(
    imageUri: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    if (imageUri.startsWith("http://") || imageUri.startsWith("https://")) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Attached receipt preview",
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }

    val context = LocalContext.current
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        withContext(Dispatchers.IO) {
            value = try {
                val cleanPath = imageUri.removePrefix("file://").removePrefix("file:")
                val file = File(cleanPath)
                if (file.exists() && file.isFile && file.length() > 0) {
                    decodeHighQualityOrientedBitmap(file.absolutePath)?.asImageBitmap()
                } else if (imageUri.startsWith("content://")) {
                    val uri = android.net.Uri.parse(imageUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } else {
                    val cacheFile = File(context.cacheDir, imageUri)
                    if (cacheFile.exists() && cacheFile.isFile && cacheFile.length() > 0) {
                        decodeHighQualityOrientedBitmap(cacheFile.absolutePath)?.asImageBitmap()
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Attached receipt preview",
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // High-fidelity fallback receipt preview graphic
        Box(
            modifier = modifier
                .background(Color(0xFF1E293B))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FairSplitColors.PrimaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = FairSplitColors.PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "BILL ATTACHED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = imageUri.substringAfterLast("/").take(24),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.LightGray
                )
            }
        }
    }
}

/**
 * Decodes bitmap at maximum sharpness with EXIF orientation correction.
 */
private fun decodeHighQualityOrientedBitmap(filePath: String): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply {
            inScaled = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inDither = true
        }
        val rawBitmap = BitmapFactory.decodeFile(filePath, options) ?: return null

        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return rawBitmap
        }

        val rotatedBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        if (rotatedBitmap != rawBitmap) {
            rawBitmap.recycle()
        }
        rotatedBitmap
    } catch (e: Exception) {
        BitmapFactory.decodeFile(filePath)
    }
}
