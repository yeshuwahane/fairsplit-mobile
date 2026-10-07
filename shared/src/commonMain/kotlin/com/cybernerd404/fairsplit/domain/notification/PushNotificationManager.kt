package com.cybernerd404.fairsplit.domain.notification

import org.koin.core.module.Module

/**
 * Platform-agnostic interface for push notification management and FCM registration tokens.
 */
interface PushNotificationManager {
    /**
     * Retrieves the current FCM token for this device.
     */
    suspend fun getFcmToken(): String?

    /**
     * Requests notification runtime permissions (Android 13+ / iOS).
     */
    suspend fun requestNotificationPermission(): Boolean

    /**
     * Initializes notification channels and listeners.
     */
    fun initialize()
}

/**
 * Platform-specific Koin module providing the platform's PushNotificationManager implementation.
 */
expect val notificationModule: Module
