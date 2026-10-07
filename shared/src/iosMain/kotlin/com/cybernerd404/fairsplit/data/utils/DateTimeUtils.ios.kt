package com.cybernerd404.fairsplit.data.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun getCurrentFormattedTime(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "h:mm a"
    formatter.locale = NSLocale.currentLocale
    return formatter.stringFromDate(NSDate())
}

actual fun getCurrentFormattedDate(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "d MMM"
    formatter.locale = NSLocale.currentLocale
    return formatter.stringFromDate(NSDate())
}

actual fun getCurrentTimestamp(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "d MMM, h:mm a"
    formatter.locale = NSLocale.currentLocale
    return formatter.stringFromDate(NSDate())
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun currentTimeMillis(): Long = platform.posix.time(null) * 1000L

