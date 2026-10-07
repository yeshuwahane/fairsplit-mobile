package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.theme.FairSplitColors
import io.github.aakira.napier.Napier
import kotlinx.cinterop.*
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.QuartzCore.CALayer
import platform.UIKit.*
import platform.darwin.*

private class QrScanDelegate(
    private val onScanned: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        didOutputMetadataObjects.firstOrNull()?.let { obj ->
            (obj as? AVMetadataMachineReadableCodeObject)?.stringValue?.let { value ->
                // Dispatch to main thread
                dispatch_async(dispatch_get_main_queue()) {
                    onScanned(value)
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrScannerScreen(
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var scanned by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        UIKitView(
            factory = {
                val containerView = UIView()
                containerView.backgroundColor = UIColor.blackColor

                val session = AVCaptureSession()
                session.sessionPreset = AVCaptureSessionPresetHigh

                val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                if (device != null) {
                    try {
                        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                        if (input != null && session.canAddInput(input)) {
                            session.addInput(input)
                        }

                        val metadataOutput = AVCaptureMetadataOutput()
                        if (session.canAddOutput(metadataOutput)) {
                            session.addOutput(metadataOutput)

                            val delegate = QrScanDelegate { value ->
                                if (!scanned) {
                                    scanned = true
                                    onCodeScanned(value)
                                }
                            }
                            metadataOutput.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
                            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
                        }

                        // Preview layer
                        val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(session)
                        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                        previewLayer.frame = containerView.bounds
                        containerView.layer.addSublayer(previewLayer)

                        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0uL)) {
                            session.startRunning()
                        }
                    } catch (e: Exception) {
                        Napier.e("QR scanner setup failed: ${e.message}")
                    }
                }
                containerView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Scanner",
                tint = Color.White
            )
        }

        // Hint
        Text(
            text = "Point camera at an Epic QR code",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}
