package com.redwater.appmonitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.redwater.appmonitor.MainActivity
import com.redwater.appmonitor.R

class ServiceNotificationManager constructor(val context: Context) {
    fun createNotification(): Notification {
        createAppMonitorServiceChannel()
        val resultIntent = Intent(context, MainActivity::class.java)
        val resultPendingIntent =
            PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, CHANNEL_ID_APP_MONITOR_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent)
            .build()
    }

    private fun createAppMonitorServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID_APP_MONITOR_SERVICE, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    companion object {
        private const val CHANNEL_ID_APP_MONITOR_SERVICE = "CHANNEL_ID_APP_MONITOR_SERVICE"
    }
}