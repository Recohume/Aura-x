package com.auramusic.app.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AuraFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token received: $token")
        AuraNotificationManager.onNewFcmToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val isUpdateType = data["type"] == "app-update" ||
                data["type"] == "release" ||
                remoteMessage.from?.contains("releases") == true

        val title = notification?.title ?: data["title"] ?: "Aura Update"
        val body = notification?.body ?: data["body"] ?: "A new version of Aura is available!"
        val version = data["version"] ?: "latest"

        if (isUpdateType) {
            AuraNotificationManager.showUpdateNotification(
                context = applicationContext,
                title = title,
                message = body,
                version = version
            )
        } else if (notification != null) {
            AuraNotificationManager.showUpdateNotification(
                context = applicationContext,
                title = title,
                message = body,
                version = version
            )
        }
    }

    companion object {
        private const val TAG = "AuraFirebaseMsg"
    }
}
