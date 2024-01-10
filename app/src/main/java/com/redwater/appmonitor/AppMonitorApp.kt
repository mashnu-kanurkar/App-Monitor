package com.redwater.appmonitor

import android.app.Application
import android.os.Build
import com.redwater.appmonitor.data.AppDatabase
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.OverlayDataRepository
import com.redwater.appmonitor.logger.LogLevel
import com.redwater.appmonitor.logger.Logger

class AppMonitorApp: Application() {

    private val TAG = this::class.simpleName

    private val database by lazy {
        AppDatabase.getInstance(applicationContext)
    }
    val appPrefsRepository by lazy{
        AppUsageStatsRepository(database.getAppPrefsDao())
    }

    val overlayDataRepository by lazy {
        OverlayDataRepository(database.getOverlayDataDao())
    }

    override fun onCreate() {
        super.onCreate()
        Logger.setLogLevel(LogLevel.DEBUG)
        Logger.d(TAG, "Build type: ${Build.TYPE}")

    }

}