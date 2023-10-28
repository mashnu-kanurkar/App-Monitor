package com.redwater.appmonitor.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.service.OverlayService
import com.redwater.appmonitor.service.ServiceManager

class ForegroundAppWorker(private val appContext: Context, private val workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    private val TAG = "ForegroundAppWorker"
    override suspend fun doWork(): Result {
        return try {
            Logger.d("$TAG => is service running ${OverlayService.isRunning}")
            if (OverlayService.isRunning.not()){
                val hasSavedAppRecords =
                    (appContext as AppMonitorApp).appPrefsRepository.getAllRecords().isNotEmpty()
                if (hasSavedAppRecords) {
                    ServiceManager.startService(applicationContext)
                }
            }
            return Result.success()
        }catch (e: Exception){
            Logger.d("$TAG => exception in doWork ${e.message}")
            Result.failure()
        }
    }
}