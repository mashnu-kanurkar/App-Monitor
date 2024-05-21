package com.redwater.appmonitor.service

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.pushnotification.NotificationWorker

class RemoteMessagingService : FirebaseMessagingService() {

    private val TAG = this::class.simpleName
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Logger.d(TAG, "Received firebase message From: ${remoteMessage.from}")
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Logger.d(TAG, "Message data payload: ${remoteMessage.data}")
            //FCMMessageHandler().createNotification(context = applicationContext, message = remoteMessage)

            val workManager = WorkManager.getInstance(applicationContext)
            val onetimePushNotificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(Data.Builder().putAll(remoteMessage.data as Map<String, Any>).build())
                .build()
            workManager.enqueueUniqueWork(System.currentTimeMillis().toString(),
                ExistingWorkPolicy.REPLACE,
                onetimePushNotificationWork )
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Logger.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.d(TAG, "token: $token")
    }

    fun getToken(context: Context): String? {
        Logger.d(TAG, "Fetching token")
        val token = context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty")
        Logger.d(TAG, "Firebase token: $token")
        return token
    }
}