package com.redwater.appmonitor.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.service.OverlayService
import com.redwater.appmonitor.service.ServiceManager

class ForegroundAppWorker(private val appContext: Context, private val workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    private val TAG = this::class.simpleName
    override suspend fun doWork(): Result {
        return try {
            Logger.d(TAG, "is service running ${OverlayService.isRunning}")
            ServiceManager.startService(appContext)
            return Result.success()
        }catch (e: Exception){
            Logger.d(TAG, "exception in doWork ${e.message}")
            Result.failure()
        }
    }
}