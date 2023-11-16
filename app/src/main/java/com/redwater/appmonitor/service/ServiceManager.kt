package com.redwater.appmonitor.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.workmanager.FirebaseSyncWorker
import com.redwater.appmonitor.workmanager.ForegroundAppWorker
import java.util.concurrent.TimeUnit

object ServiceManager {

    private val TAG = this::class.simpleName?:"Service Manager"

    fun startService(context: Context, intent: Intent = Intent(context, OverlayService::class.java)) {
        Logger.d(TAG, "starting service")

        if (OverlayService.isRunning.not()){
            val hasSavedAppRecords = true

            Logger.d(TAG, "$hasSavedAppRecords")
            if (hasSavedAppRecords) {
                val permissionManager = PermissionManager()
                if (permissionManager.hasOverlayPermission(context) && permissionManager.hasNotificationPermission(context)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context.applicationContext, intent)
                    } else {
                        context.startService(intent)
                    }
                    val foregroundAppWorkRequest = PeriodicWorkRequestBuilder<ForegroundAppWorker>(Constants.foregroundAppWorkerPeriodInMin, TimeUnit.MINUTES).build()
                    WorkManager.getInstance(context.applicationContext)
                        .enqueueUniquePeriodicWork(Constants.foregroundAppWorkerTag, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, foregroundAppWorkRequest)

                    val firebaseSyncWorkRequest = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(Constants.firebaseSyncWorkerPeriodInHour, TimeUnit.MINUTES).build()
                    WorkManager.getInstance(context.applicationContext)
                        .enqueueUniquePeriodicWork(Constants.firebaseSyncWorkerTag, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, firebaseSyncWorkRequest)
                }
            }
        }
    }

    fun stopService(context: Context, intent: Intent = Intent(context, OverlayService::class.java)){
        Logger.d(TAG, "stopping service")
        context.stopService(intent)
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(Constants.foregroundAppWorkerTag)
    }
}