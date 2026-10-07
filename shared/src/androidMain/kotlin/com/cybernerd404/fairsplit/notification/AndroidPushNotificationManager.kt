package com.cybernerd404.fairsplit.domain.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.coroutines.resume

class AndroidPushNotificationManager : PushNotificationManager {

    override suspend fun getFcmToken(): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            // [DEVELOPMENT ONLY] - Log token for manual testing and backend registration
                            Log.d("FAIRSPLIT_FCM", "==================================================")
                            Log.d("FAIRSPLIT_FCM", "🔥 [DEVELOPMENT ONLY] RETRIEVED FCM TOKEN: $token")
                            Log.d("FAIRSPLIT_FCM", "==================================================")
                            Napier.i(tag = "FAIRSPLIT_FCM", message = "FCM Token retrieved successfully")
                            continuation.resume(token)
                        } else {
                            val exception = task.exception
                            Napier.w(
                                tag = "FAIRSPLIT_FCM",
                                message = "Failed to retrieve FCM token: ${exception?.message}",
                                throwable = exception
                            )
                            continuation.resume(null)
                        }
                    }
            } catch (e: Exception) {
                Napier.e(tag = "FAIRSPLIT_FCM", message = "FCM token retrieval error: ${e.message}", throwable = e)
                continuation.resume(null)
            }
        }
    }

    override suspend fun requestNotificationPermission(): Boolean {
        // Handled via Android 13 POST_NOTIFICATIONS permission request in Activity/Compose
        return true
    }

    override fun initialize() {
        Napier.d(tag = "FAIRSPLIT_FCM", message = "AndroidPushNotificationManager initialized")
    }
}

actual val notificationModule: Module = module {
    single<PushNotificationManager> { AndroidPushNotificationManager() }
}
