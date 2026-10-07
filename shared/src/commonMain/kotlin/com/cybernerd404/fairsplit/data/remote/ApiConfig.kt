package com.cybernerd404.fairsplit.data.remote

import com.cybernerd404.fairsplit.getPlatform

/**
 * Centralized API configuration for FairSplit backend connectivity.
 * Points to the production cloud backend hosted on Railway.
 */
object ApiConfig {

    /**
     * Hosted production backend on Railway.
     */
    const val PRODUCTION_URL = "https://fairplit-backend-production.up.railway.app"

    /**
     * Set to true to test against local Ktor backend during local development if needed.
     * Default is false (production hosted backend).
     */
    var useLocalServer: Boolean = false

    /**
     * Development host configuration.
     * Android emulator maps host's localhost to 10.0.2.2.
     * iOS simulator / Desktop connects to localhost directly.
     * For physical devices over Wi-Fi, set [customHost] to your machine's Wi-Fi IP.
     */
    const val ANDROID_EMULATOR_HOST = "10.0.2.2"
    const val LAN_HOST = "192.168.1.4"
    const val LOCALHOST = "localhost"
    const val LOCAL_PORT = "8080"

    /**
     * Set this if testing on a physical phone over a different Wi-Fi network or adb reverse.
     */
    var customHost: String? = null

    val defaultLocalHost: String
        get() {
            val platform = getPlatform()
            return when {
                platform.isEmulator && platform.name.lowercase().contains("android") -> ANDROID_EMULATOR_HOST
                platform.isEmulator -> LOCALHOST
                else -> LAN_HOST
            }
        }

    val baseUrl: String
        get() = if (useLocalServer) {
            val host = customHost ?: defaultLocalHost
            "http://$host:$LOCAL_PORT"
        } else {
            PRODUCTION_URL
        }

    val webSocketBaseUrl: String
        get() = if (useLocalServer) {
            val host = customHost ?: defaultLocalHost
            "ws://$host:$LOCAL_PORT"
        } else {
            PRODUCTION_URL.replace("http://", "ws://").replace("https://", "wss://")
        }

    // Standard API path prefixes
    const val API_V1_PREFIX = "/api/v1"

    // Convenience endpoint URLs
    val healthUrl: String get() = "$baseUrl/health"
    val requestOtpUrl: String get() = "$baseUrl$API_V1_PREFIX/auth/phone/request-otp"
    val verifyOtpUrl: String get() = "$baseUrl$API_V1_PREFIX/auth/phone/verify-otp"
    val refreshTokenUrl: String get() = "$baseUrl$API_V1_PREFIX/auth/refresh"
    val logoutUrl: String get() = "$baseUrl$API_V1_PREFIX/auth/logout"

    fun epicWebSocketUrl(epicId: String): String {
        return "$webSocketBaseUrl$API_V1_PREFIX/ws/epics/$epicId"
    }
}
