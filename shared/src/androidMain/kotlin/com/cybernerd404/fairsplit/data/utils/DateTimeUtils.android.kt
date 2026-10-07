package com.cybernerd404.fairsplit.data.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun getCurrentFormattedTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

actual fun getCurrentFormattedDate(): String {
    val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
    return sdf.format(Date())
}

actual fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
    return sdf.format(Date())
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

