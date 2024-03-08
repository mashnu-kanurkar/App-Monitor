package com.redwater.appmonitor

import android.app.Application
import android.os.Build
import android.os.Debug
import com.redwater.appmonitor.data.AppDatabase
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.OverlayDataRepository
import com.redwater.appmonitor.logger.LogLevel
import com.redwater.appmonitor.logger.Logger
import io.grpc.android.BuildConfig

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

        Logger.i(TAG, "Debug Build : ${BuildConfig.DEBUG}")
        Logger.setLogLevel(LogLevel.DEBUG)
//        if (BuildConfig.DEBUG){
//            Logger.setLogLevel(LogLevel.DEBUG)
//        }else{
//            Logger.setLogLevel(LogLevel.OFF)
//        }



    }

}