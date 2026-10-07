package com.cybernerd404.fairsplit.domain.notification

import io.github.aakira.napier.Napier
import org.koin.core.module.Module
import org.koin.dsl.module

class IosPushNotificationManager : PushNotificationManager {

    private var cachedToken: String? = null

    fun updateApnsFcmToken(token: String) {
        cachedToken = token
        // [DEVELOPMENT ONLY] - Log token for manual testing
        Napier.i(tag = "FAIRSPLIT_FCM_IOS", message = "🔥 [DEVELOPMENT ONLY] NEW IOS FCM TOKEN: $token")
    }

    override suspend fun getFcmToken(): String? {
        return cachedToken
    }

    override suspend fun requestNotificationPermission(): Boolean {
        Napier.d(tag = "FAIRSPLIT_FCM_IOS", message = "Requesting iOS UNUserNotificationCenter permissions")
        return true
    }

    override fun initialize() {
        Napier.d(tag = "FAIRSPLIT_FCM_IOS", message = "IosPushNotificationManager initialized")
    }
}

actual val notificationModule: Module = module {
    single<PushNotificationManager> { IosPushNotificationManager() }
}
