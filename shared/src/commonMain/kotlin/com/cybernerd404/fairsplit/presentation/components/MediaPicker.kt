package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.runtime.Composable

enum class ImageSource {
    CAMERA,
    GALLERY
}

interface MediaPickerLauncher {
    fun launch(source: ImageSource)
}

@Composable
expect fun rememberMediaPickerLauncher(
    onImagePicked: (uri: String) -> Unit,
    onPermissionDenied: ((permission: String) -> Unit)? = null
): MediaPickerLauncher
