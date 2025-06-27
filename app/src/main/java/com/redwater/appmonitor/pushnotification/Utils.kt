package com.redwater.appmonitor.pushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationManagerCompat
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger

/**
 * Retrieves or creates the notification channel based on the given channel ID.
 * If the given channel ID is not registered, it falls back to the manifest channel ID.
 * If the manifest channel ID is not registered or not available, it creates and returns the default channel ID.
 *
 * @param msgChannel The channel ID received in the push payload.
 * @param context The context of the application.
 * @return The channel ID of the notification channel to be used.
 */
@RequiresApi(Build.VERSION_CODES.O)
@WorkerThread
fun NotificationManagerCompat.getOrCreateChannel(msgChannel: String?, context: Context): String? {
    try {
        /**
         * if channel id is present in push payload and registered by an app then return the payload channel id
         */
        if (!msgChannel.isNullOrEmpty() && getNotificationChannel(msgChannel) != null) {
            return msgChannel
        }

        /**
         * create fallback channel
         */
        if (getNotificationChannel(Constants.FCM_FALLBACK_NOTIFICATION_CHANNEL_ID) == null) {
            val defaultChannelName = try {
                context.getString(R.string.ct_fcm_fallback_notification_channel_label)
            } catch (e: Exception) {
                Constants.FCM_FALLBACK_NOTIFICATION_CHANNEL_NAME
            }

            createNotificationChannel(
                NotificationChannel(
                    Constants.FCM_FALLBACK_NOTIFICATION_CHANNEL_ID,
                    defaultChannelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).also {
                    Logger.d("getOrCreateChannel", "created default channel: $it")
                }
            )
        }
        return Constants.FCM_FALLBACK_NOTIFICATION_CHANNEL_ID
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun Context.isNotificationChannelEnabled(channelId: String): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        areAppNotificationsEnabled() && try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.getNotificationChannel(channelId).importance != NotificationManager.IMPORTANCE_NONE
        } catch (e: Exception) {
            Logger.d("isNotificationChannelEnabled","Unable to find notification channel with id = $channelId")
            false
        }
    } else {
        areAppNotificationsEnabled()
    }

fun Context.areAppNotificationsEnabled() = try {
    NotificationManagerCompat.from(this).areNotificationsEnabled()
} catch (e: Exception) {
    Logger.d("areAppNotificationsEnabled","Unable to query notifications enabled flag, returning true!")
    e.printStackTrace()
    true
}

object NotificationUtils {

    //Require to close notification on action button click
    fun dismissNotification(intent: Intent?, applicationContext: Context){
        intent?.extras?.apply {
            var autoCancel = true
            var notificationId = -1

            getString("actionId")?.let {
                Log.d("ACTION_ID", it)
                autoCancel = getBoolean("autoCancel", true)
                notificationId = getInt("notificationId", -1)
            }
            /**
             * If using InputBox template, add ptDismissOnClick flag to not dismiss notification
             * if pt_dismiss_on_click is false in InputBox template payload. Alternatively if normal
             * notification is raised then we dismiss notification.
             */
            //val ptDismissOnClick = intent.extras!!.getString(PTConstants.PT_DISMISS_ON_CLICK,"")

            if (autoCancel && notificationId > -1) {
                val notifyMgr: NotificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notifyMgr.cancel(notificationId)
            }
        }
    }
}



