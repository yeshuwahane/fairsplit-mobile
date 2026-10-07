package com.cybernerd404.fairsplit.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Mobile-side representation for registering an FCM push notification token with the future backend.
 */
@Serializable
data class DeviceRegistrationDto(
    val token: String,
    val platform: String, // "android" or "ios"
    val appVersion: String = "1.0.0",
    val deviceModel: String? = null
)
