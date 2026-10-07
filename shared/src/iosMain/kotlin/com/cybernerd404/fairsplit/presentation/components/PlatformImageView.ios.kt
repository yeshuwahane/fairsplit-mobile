package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cybernerd404.fairsplit.theme.FairSplitColors

@Composable
actual fun PlatformImageView(
    imageUri: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    if (imageUri.isNotBlank() && (imageUri.startsWith("http://") || imageUri.startsWith("https://") || imageUri.startsWith("file://") || imageUri.startsWith("/"))) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Attached receipt preview",
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = FairSplitColors.PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
