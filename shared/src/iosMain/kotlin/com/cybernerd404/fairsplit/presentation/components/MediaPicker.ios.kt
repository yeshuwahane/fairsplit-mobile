package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.cybernerd404.fairsplit.data.remote.datasource.RemoteSyncDataSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

// Strong reference to prevent ARC deallocation while UIImagePickerController is active
private var activePickerDelegate: NSObject? = null

@Composable
actual fun rememberMediaPickerLauncher(
    onImagePicked: (uri: String) -> Unit,
    onPermissionDenied: ((permission: String) -> Unit)?
): MediaPickerLauncher {
    val coroutineScope = rememberCoroutineScope()
    val syncDataSource = remember {
        try {
            KoinPlatformTools.defaultContext().get().getOrNull<RemoteSyncDataSource>()
        } catch (e: Exception) {
            null
        }
    }

    return remember(coroutineScope, syncDataSource) {
        object : MediaPickerLauncher {
            override fun launch(source: ImageSource) {
                when (source) {
                    ImageSource.GALLERY -> {
                        val status = PHPhotoLibrary.authorizationStatus()
                        if (status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited) {
                            presentImagePicker(
                                source = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary,
                                coroutineScope = coroutineScope,
                                syncDataSource = syncDataSource,
                                onImagePicked = onImagePicked
                            )
                        } else if (status == PHAuthorizationStatusNotDetermined) {
                            PHPhotoLibrary.requestAuthorization { newStatus ->
                                dispatch_async(dispatch_get_main_queue()) {
                                    if (newStatus == PHAuthorizationStatusAuthorized || newStatus == PHAuthorizationStatusLimited) {
                                        presentImagePicker(
                                            source = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary,
                                            coroutineScope = coroutineScope,
                                            syncDataSource = syncDataSource,
                                            onImagePicked = onImagePicked
                                        )
                                    } else {
                                        onPermissionDenied?.invoke("Photo Library")
                                    }
                                }
                            }
                        } else {
                            onPermissionDenied?.invoke("Photo Library")
                        }
                    }
                    ImageSource.CAMERA -> {
                        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                            onPermissionDenied?.invoke("Camera Not Available")
                            return
                        }
                        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                        if (status == AVAuthorizationStatusAuthorized) {
                            presentImagePicker(
                                source = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                                coroutineScope = coroutineScope,
                                syncDataSource = syncDataSource,
                                onImagePicked = onImagePicked
                            )
                        } else if (status == AVAuthorizationStatusNotDetermined) {
                            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                                dispatch_async(dispatch_get_main_queue()) {
                                    if (granted) {
                                        presentImagePicker(
                                            source = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                                            coroutineScope = coroutineScope,
                                            syncDataSource = syncDataSource,
                                            onImagePicked = onImagePicked
                                        )
                                    } else {
                                        onPermissionDenied?.invoke("Camera")
                                    }
                                }
                            }
                        } else {
                            onPermissionDenied?.invoke("Camera")
                        }
                    }
                }
            }
        }
    }
}

private fun getTopViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val keyWindow = app.windows.mapNotNull { it as? UIWindow }.firstOrNull { it.isKeyWindow() }
        ?: app.keyWindow
    var topController = keyWindow?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }
    return topController
}

private fun presentImagePicker(
    source: UIImagePickerControllerSourceType,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    syncDataSource: RemoteSyncDataSource?,
    onImagePicked: (String) -> Unit
) {
    dispatch_async(dispatch_get_main_queue()) {
        val rootViewController = getTopViewController() ?: return@dispatch_async
        val picker = UIImagePickerController()
        picker.sourceType = source

        val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                val timestamp = com.cybernerd404.fairsplit.data.utils.currentTimeMillis()
                val fileName = "receipt_ios_${timestamp}.jpg"

                if (image != null) {
                    val nsData = UIImageJPEGRepresentation(image, 0.75)
                    val base64 = nsData?.base64EncodedStringWithOptions(0u)
                    if (base64 != null && syncDataSource != null) {
                        coroutineScope.launch(Dispatchers.Default) {
                            val remoteUrl = syncDataSource.uploadImage(fileName, base64, "image/jpeg")
                            dispatch_async(dispatch_get_main_queue()) {
                                onImagePicked(remoteUrl ?: fileName)
                            }
                        }
                    } else {
                        onImagePicked(fileName)
                    }
                } else {
                    onImagePicked(fileName)
                }

                picker.dismissViewControllerAnimated(true) {
                    activePickerDelegate = null
                }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true) {
                    activePickerDelegate = null
                }
            }
        }

        activePickerDelegate = delegate
        picker.delegate = delegate
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
}
