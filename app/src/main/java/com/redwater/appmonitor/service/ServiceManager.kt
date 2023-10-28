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
import com.redwater.appmonitor.workmanager.ForegroundAppWorker
import java.util.concurrent.TimeUnit

object ServiceManager {

    private const val TAG = "ServiceStarter"

    fun startService(context: Context, intent: Intent = Intent(context, OverlayService::class.java)) {
        Logger.d("$TAG => starting service")
        val permissionManager = PermissionManager()
        if (permissionManager.hasOverlayPermission(context) && permissionManager.hasNotificationPermission(context)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
            val workRequest = PeriodicWorkRequestBuilder<ForegroundAppWorker>(Constants.foregroundAppWorkerPeriodInMin, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(Constants.foregroundAppWorkerTag, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest)
        }
    }

    fun stopService(context: Context, intent: Intent = Intent(context, OverlayService::class.java)){
        Logger.d("$TAG => starting service")
        context.stopService(intent)
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(Constants.foregroundAppWorkerTag)
    }
}