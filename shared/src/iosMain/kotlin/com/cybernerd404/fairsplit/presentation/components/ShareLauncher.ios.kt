package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberShareLauncher(): ShareLauncher {
    return remember {
        object : ShareLauncher {
            override fun share(text: String, title: String) {
                dispatch_async(dispatch_get_main_queue()) {
                    val app = UIApplication.sharedApplication
                    val keyWindow = app.windows.mapNotNull { it as? UIWindow }.firstOrNull { it.isKeyWindow() }
                        ?: app.keyWindow
                    var topController: UIViewController? = keyWindow?.rootViewController
                    while (topController?.presentedViewController != null) {
                        topController = topController.presentedViewController
                    }

                    if (topController == null) return@dispatch_async

                    val activityViewController = UIActivityViewController(
                        activityItems = listOf(text),
                        applicationActivities = null
                    )

                    activityViewController.popoverPresentationController?.apply {
                        sourceView = topController.view
                        topController.view?.bounds?.let { bounds ->
                            sourceRect = bounds
                        }
                    }

                    topController.presentViewController(
                        viewControllerToPresent = activityViewController,
                        animated = true,
                        completion = null
                    )
                }
            }
        }
    }
}

