package com.redwater.appmonitor.pushnotification

import android.graphics.Bitmap
import android.graphics.Color
import com.redwater.appmonitor.Constants
import java.lang.NumberFormatException

class NotificationPayloadParser(private val messageData: Map<String, Any>) {

    fun getTitle(): String? {
        val title = messageData.get(Constants.NOTIF_TITLE)
        return title?.toString()
    }

    fun getMessage(): String? {
        val message = messageData.get(Constants.NOTIF_MSG)
        return message?.toString()
    }

    suspend fun getBigPicture(): Bitmap? {
        val bigPictureUrl =  messageData.get(Constants.BIG_PICTURE)
        return if (bigPictureUrl != null){
            NotificationBitmapDownloader(bigPictureUrl.toString()).getBitmap()
        }else{
            null
        }
    }

    fun getBackgroundColor(): Int? {
        val color = messageData.get(Constants.BG_COLOR)
        return if (color != null){
            Color.parseColor(color.toString())
        }else{
            null
        }
    }

    fun getDeeplink(): String? {
        val deeplink = messageData.get(Constants.DEEPLINK)
        return deeplink?.toString()
    }

    fun getChannelID(): String? {
        val channelId = messageData.get(Constants.NOTIF_CHANNEL)
        return channelId?.toString()
    }

    fun getPriority(): Int {
        val priority = messageData.get(Constants.NOTIF_PRIORITY)
        return try {
            priority?.toString()?.toInt()?:Constants.PRIORITY_MAX
        }catch (e: NumberFormatException){
            e.printStackTrace()
            Constants.PRIORITY_MAX
        }
    }
}