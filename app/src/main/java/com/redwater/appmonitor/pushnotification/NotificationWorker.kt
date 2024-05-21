package com.redwater.appmonitor.pushnotification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redwater.appmonitor.logger.Logger

class NotificationWorker(private val context: Context,
                         private val workerParameters: WorkerParameters):
    CoroutineWorker(context, workerParameters) {
        private val TAG = this::class.simpleName
    override suspend fun doWork(): Result {
        val data = inputData.keyValueMap
        Logger.d(TAG, "input data: $data")
        val notificationRenderer = NotificationRenderer(data, context)
        notificationRenderer.render()
        return Result.success()
    }
}