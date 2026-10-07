package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun PlatformImageView(
    imageUri: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
)
