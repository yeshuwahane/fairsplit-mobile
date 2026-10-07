package com.cybernerd404.fairsplit.data.local

import com.russhwolf.settings.Settings

/**
 * Clean abstraction for storing and retrieving JWT authentication tokens.
 * Backed by Multiplatform Settings (Encrypted/Platform Preferences).
 */
interface TokenStorage {
    fun getAccessToken(): String?
    fun setAccessToken(token: String?)
    fun getRefreshToken(): String?
    fun setRefreshToken(token: String?)
    fun clearTokens()
    fun hasValidSession(): Boolean
}

class SettingsTokenStorage(
    private val settings: Settings = Settings()
) : TokenStorage {

    companion object {
        private const val KEY_ACCESS_TOKEN = "jwt_access_token"
        private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
    }

    override fun getAccessToken(): String? = settings.getStringOrNull(KEY_ACCESS_TOKEN)

    override fun setAccessToken(token: String?) {
        if (token != null) {
            settings.putString(KEY_ACCESS_TOKEN, token)
        } else {
            settings.remove(KEY_ACCESS_TOKEN)
        }
    }

    override fun getRefreshToken(): String? = settings.getStringOrNull(KEY_REFRESH_TOKEN)

    override fun setRefreshToken(token: String?) {
        if (token != null) {
            settings.putString(KEY_REFRESH_TOKEN, token)
        } else {
            settings.remove(KEY_REFRESH_TOKEN)
        }
    }

    override fun clearTokens() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    override fun hasValidSession(): Boolean = !getAccessToken().isNullOrBlank()
}
