package com.cybernerd404.fairsplit.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.cybernerd404.fairsplit.theme.FairSplitColors
import io.github.aakira.napier.Napier
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
actual fun QrScannerScreen(
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scanned by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            // Camera preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val barcodeScanner = BarcodeScanning.getClient()
                        val analysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(executor) { imageProxy ->
                            @androidx.camera.core.ExperimentalGetImage
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !scanned) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull { it.rawValue != null }?.let { barcode ->
                                            barcode.rawValue?.let { raw ->
                                                scanned = true
                                                onCodeScanned(raw)
                                            }
                                        }
                                    }
                                    .addOnFailureListener { Napier.e("Barcode scan failed: ${it.message}") }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        } catch (e: Exception) {
                            Napier.e("Camera binding failed: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scan frame overlay — corner brackets
            val cornerColor = FairSplitColors.AccentCyan
            val strokeW = 4.dp
            val cornerLen = 40.dp
            Canvas(modifier = Modifier.size(240.dp)) {
                val cL = cornerLen.toPx()
                val sW = strokeW.toPx()
                // top-left
                drawLine(cornerColor, Offset(0f, cL), Offset(0f, 0f), sW)
                drawLine(cornerColor, Offset(0f, 0f), Offset(cL, 0f), sW)
                // top-right
                drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width - cL, 0f), sW)
                drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width, cL), sW)
                // bottom-left
                drawLine(cornerColor, Offset(0f, size.height), Offset(0f, size.height - cL), sW)
                drawLine(cornerColor, Offset(0f, size.height), Offset(cL, size.height), sW)
                // bottom-right
                drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width - cL, size.height), sW)
                drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width, size.height - cL), sW)
            }

            // Hint text
            Text(
                text = "Point camera at an Epic QR code",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission required", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }

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
    }
}
