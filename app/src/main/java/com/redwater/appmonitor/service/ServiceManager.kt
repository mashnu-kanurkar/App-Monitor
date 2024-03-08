package com.redwater.appmonitor.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.workmanager.FirebaseSyncWorker
import com.redwater.appmonitor.workmanager.ForegroundAppWorker
import java.util.concurrent.TimeUnit

object ServiceManager {

    private val TAG = this::class.simpleName?:"Service Manager"

    fun startService(context: Context, intent: Intent = Intent(context, OverlayService::class.java,)) {
        Logger.d(TAG, "starting service")

        if (OverlayService.isRunning.value.not()) {

            val permissionManager = PermissionManager()
            if (permissionManager.hasOverlayPermission(context) && permissionManager.hasNotificationPermission(
                    context
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context.applicationContext, intent)
                } else {
                    context.startService(intent)
                }
                val foregroundAppWorkRequest = PeriodicWorkRequestBuilder<ForegroundAppWorker>(
                    Constants.foregroundAppWorkerPeriodInMin,
                    TimeUnit.MINUTES
                ).build()
                WorkManager.getInstance(context.applicationContext)
                    .enqueueUniquePeriodicWork(
                        Constants.foregroundAppWorkerTag,
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        foregroundAppWorkRequest
                    )

//                val firebaseSyncWorkRequest = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(
//                    Constants.firebaseSyncWorkerPeriodInHour,
//                    TimeUnit.MINUTES
//                ).build()
//                WorkManager.getInstance(context.applicationContext)
//                    .enqueueUniquePeriodicWork(
//                        Constants.firebaseSyncWorkerTag,
//                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
//                        firebaseSyncWorkRequest
//                    )
            }
        }
    }

    private suspend fun stopService(context: Context, intent: Intent = Intent(context, OverlayService::class.java), repository: AppUsageStatsRepository){
        if (repository.getAllSelectedRecords().isEmpty()){
            Logger.d(TAG, "stopping service")
            context.stopService(intent)
            WorkManager.getInstance(context.applicationContext)
                .cancelUniqueWork(Constants.foregroundAppWorkerTag)
        }
    }
    suspend fun toggleService(context: Context, intent: Intent = Intent(context, OverlayService::class.java), repository: AppUsageStatsRepository){
        if (repository.getAllSelectedRecords().isEmpty()){
            stopService(context = context, intent = intent, repository = repository)
        }else{
            startService(context = context, intent = intent)
        }
    }

}