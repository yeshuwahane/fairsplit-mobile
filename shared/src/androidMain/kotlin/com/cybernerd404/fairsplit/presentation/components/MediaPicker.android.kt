package com.cybernerd404.fairsplit.presentation.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.mp.KoinPlatformTools
import java.io.File
import java.io.FileOutputStream

private const val PREFS_MEDIA = "fairsplit_media_prefs"
private const val KEY_PENDING_CAM = "key_pending_camera_path"

@Composable
actual fun rememberMediaPickerLauncher(
    onImagePicked: (uri: String) -> Unit,
    onPermissionDenied: ((permission: String) -> Unit)?
): MediaPickerLauncher {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentCameraPath by remember { mutableStateOf<String?>(null) }

    val syncDataSource = remember {
        try {
            KoinPlatformTools.defaultContext().get().getOrNull<RemoteSyncDataSource>()
        } catch (e: Exception) {
            null
        }
    }

    // Helper to upload image in background and return URL
    fun uploadAndDeliverImage(file: File, fallbackUri: String? = null) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (file.exists() && file.length() > 0) {
                    val bytes = file.readBytes()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    val remoteUrl = syncDataSource?.uploadImage(file.name, base64, "image/jpeg")
                    withContext(Dispatchers.Main) {
                        onImagePicked(remoteUrl ?: file.absolutePath)
                    }
                    return@launch
                }
            } catch (e: Exception) {
                Napier.w("Image upload failed: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                onImagePicked(if (file.exists()) file.absolutePath else fallbackUri ?: "")
            }
        }
    }

    // Process picked gallery Uri safely
    fun handlePickedGalleryUri(uri: Uri?) {
        if (uri == null) return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val tempFile = File(context.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                uploadAndDeliverImage(tempFile, uri.toString())
            } catch (e: Exception) {
                Napier.e("Failed to process picked gallery image: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onImagePicked(uri.toString())
                }
            }
        }
    }

    // Modern Photo Picker (API 30+)
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        handlePickedGalleryUri(uri)
    }

    // Legacy GetContent fallback
    val galleryFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        handlePickedGalleryUri(uri)
    }

    // High-Resolution Native Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val prefs = context.getSharedPreferences(PREFS_MEDIA, Context.MODE_PRIVATE)
        val savedPath = prefs.getString(KEY_PENDING_CAM, null)
        val path = savedPath ?: currentCameraPath

        if (success && path != null) {
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                uploadAndDeliverImage(file)
            }
        }
    }

    fun launchCameraInternal() {
        try {
            val storageDir = File(context.cacheDir, "camera_receipts")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val photoFile = File.createTempFile(
                "receipt_cam_${System.currentTimeMillis()}_",
                ".jpg",
                storageDir
            )
            currentCameraPath = photoFile.absolutePath
            context.getSharedPreferences(PREFS_MEDIA, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PENDING_CAM, photoFile.absolutePath)
                .apply()

            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Napier.e("Failed to launch camera: ${e.message}", e)
        }
    }

    // Permission launcher for Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraInternal()
        } else {
            onPermissionDenied?.invoke("Camera")
        }
    }

    return remember(pickVisualMediaLauncher, galleryFallbackLauncher, cameraLauncher, permissionLauncher, context) {
        object : MediaPickerLauncher {
            override fun launch(source: ImageSource) {
                when (source) {
                    ImageSource.GALLERY -> {
                        try {
                            pickVisualMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } catch (e: Exception) {
                            try {
                                galleryFallbackLauncher.launch("image/*")
                            } catch (e2: Exception) {
                                Napier.e("Failed to launch gallery: ${e2.message}", e2)
                            }
                        }
                    }
                    ImageSource.CAMERA -> {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            launchCameraInternal()
                        } else {
                            try {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            } catch (e: Exception) {
                                Napier.e("Failed to request camera permission: ${e.message}", e)
                            }
                        }
                    }
                }
            }
        }
    }
}
