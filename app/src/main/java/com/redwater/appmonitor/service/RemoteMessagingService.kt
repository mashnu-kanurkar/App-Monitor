package com.redwater.appmonitor.service

import android.content.Context
import android.os.Bundle
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
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
            val extras = Bundle()
            for ((key, value) in remoteMessage.data) {
                extras.putString(key, value)
            }
            val info = CleverTapAPI.getNotificationInfo(extras)
            if (info.fromCleverTap) {
                CTFcmMessageHandler().createNotification(applicationContext, remoteMessage);
            } else {
                val workManager = WorkManager.getInstance(applicationContext)
                val onetimePushNotificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInputData(Data.Builder().putAll(remoteMessage.data as Map<String, Any>).build())
                    .build()
                workManager.enqueueUniqueWork(System.currentTimeMillis().toString(),
                    ExistingWorkPolicy.REPLACE,
                    onetimePushNotificationWork )
            }
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