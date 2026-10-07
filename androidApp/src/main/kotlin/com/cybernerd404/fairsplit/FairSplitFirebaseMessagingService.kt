package com.cybernerd404.fairsplit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FairSplitFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // [DEVELOPMENT ONLY] - Log token to console for inspection and testing
        Log.d("FAIRSPLIT_FCM", "==================================================")
        Log.d("FAIRSPLIT_FCM", "🔥 [DEVELOPMENT ONLY] NEW FCM TOKEN: $token")
        Log.d("FAIRSPLIT_FCM", "==================================================")
        println("🔥 [DEVELOPMENT ONLY] NEW FCM TOKEN: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "FairSplit Update"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "New activity in your group!"

        val rawChannel = remoteMessage.notification?.channelId
            ?: remoteMessage.data["channel_id"]
            ?: remoteMessage.data["android_channel_id"]
            ?: remoteMessage.from?.substringAfterLast("/topics/")
            ?: "general"

        val channelId = when (rawChannel) {
            "expenses", "new_expense", "expense_update" -> "fairsplit_expenses"
            "settlements", "settlement_paid", "settlement_requested" -> "fairsplit_settlements"
            "epics", "epic_invite", "group_update" -> "fairsplit_epics"
            else -> "fairsplit_general"
        }

        Log.d("FAIRSPLIT_FCM", "Received Notification - Channel: $channelId, Title: $title, Body: $body")
        showNotification(title, body, channelId)
    }

    private fun showNotification(title: String, body: String, channelId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createAllNotificationChannels(notificationManager)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createAllNotificationChannels(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel("fairsplit_expenses", "Expense Updates", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications for new expenses and expense updates"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel("fairsplit_settlements", "Settlement & Balances", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifications for debt settlements and payments"
                    enableLights(true)
                    enableVibration(true)
                },
                NotificationChannel("fairsplit_epics", "Epic / Group Activity", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Invites and activity updates in your groups"
                },
                NotificationChannel("fairsplit_general", "General Announcements", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "General FairSplit notifications"
                }
            )
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
