package com.redwater.appmonitor.pushnotification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.MainActivity
import com.redwater.appmonitor.R


class NotificationRenderer(private val remoteMessageData: Map<String, Any>,
                           private val context: Context,) {

    private val NOTIFICATION_ID = 1000
    suspend fun render(){
        if (remoteMessageData.isNotEmpty()){
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationPayloadParser = NotificationPayloadParser(messageData = remoteMessageData)
            var channelID: String? = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                channelID = notificationManager.getOrCreateChannel( notificationPayloadParser.getChannelID(), context)
            }
            if (channelID == null){
                channelID = Constants.CHANNEL_ID
            }
            try {

                val notification = NotificationCompat.Builder(context, channelID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notificationPayloadParser.getTitle())
                    .setContentText(notificationPayloadParser.getMessage())
                    .setPriority(notificationPayloadParser.getPriority())
                NotificationCompat.PRIORITY_MAX

                val image = notificationPayloadParser.getBigPicture()
                if (image != null){
                    notification.setLargeIcon(image)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                            .bigPicture(image).bigLargeIcon(image))
                }
                val bgColor = notificationPayloadParser.getBackgroundColor()
                bgColor?.let {
                    notification.color = bgColor
                }

                val deeplink = notificationPayloadParser.getDeeplink()

                if (deeplink != null){
                    // Create an explicit intent for an Activity in your app.
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplink)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent =
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    notification.setContentIntent(pendingIntent)
                }else{
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent =
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    notification.setContentIntent(pendingIntent)
                }
                notification.build()

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }
}