package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific QR code scanner composable.
 * - Android: uses CameraX + ML Kit BarcodeScanner
 * - iOS: uses AVFoundation
 *
 * [onCodeScanned] is called once when a valid QR/barcode is detected.
 * [onDismiss] is called when the user dismisses/cancels the scanner.
 */
@Composable
expect fun QrScannerScreen(
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
)
