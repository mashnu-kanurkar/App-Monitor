package com.redwater.appmonitor.pushnotification

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.net.URL

class NotificationBitmapDownloader(private val srcUrl: String) {

    suspend fun getBitmap(): Bitmap? {
        return CoroutineScope(Dispatchers.IO).async {
            return@async downloadBitmap()
        }.await()
    }

    private fun downloadBitmap(): Bitmap? {
        val url = URL(srcUrl)
        return BitmapFactory.decodeStream(url.openConnection().getInputStream())
    }
}