package com.cybernerd404.fairsplit

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isEmulator: Boolean = (
        Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || "google_sdk" == Build.PRODUCT
        || Build.HARDWARE.contains("goldfish")
        || Build.HARDWARE.contains("ranchu")
    )
}

actual fun getPlatform(): Platform = AndroidPlatform()