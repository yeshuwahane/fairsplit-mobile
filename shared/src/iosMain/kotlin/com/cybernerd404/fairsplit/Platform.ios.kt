package com.cybernerd404.fairsplit

import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isEmulator: Boolean
        get() = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null ||
                NSProcessInfo.processInfo.environment["SIMULATOR_UDID"] != null
}

actual fun getPlatform(): Platform = IOSPlatform()