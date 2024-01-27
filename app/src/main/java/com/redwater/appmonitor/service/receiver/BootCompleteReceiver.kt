package com.redwater.appmonitor.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.redwater.appmonitor.data.AppDatabase
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.service.ServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompleteReceiver : BroadcastReceiver() {
    private val TAG = "BootCompleteReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            context?.let {
                Logger.d(TAG, "Boot complete receiver invoked")
                CoroutineScope(Dispatchers.Default).launch {
                    val database by lazy {
                        AppDatabase.getInstance(context.applicationContext)
                    }
                    val appPrefsRepository by lazy{
                        AppUsageStatsRepository(database.getAppPrefsDao())
                    }
                    val hasRecords = appPrefsRepository.getAllSelectedRecords().isNotEmpty()
                    if (hasRecords){
                        ServiceManager.startService(context)
                    }else{
                        Logger.d(TAG, "Empty preferences, not starting a service")
                    }
                }
            }
        }
    }
}