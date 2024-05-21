package com.redwater.appmonitor

import android.app.Application
import com.redwater.appmonitor.data.AppDatabase
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.BlogRepository
import com.redwater.appmonitor.data.repository.QuotesRepository
import com.redwater.appmonitor.logger.LogLevel
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.service.RemoteMessagingService
import io.grpc.android.BuildConfig

class AppMonitorApp: Application() {

    private val TAG = this::class.simpleName

    private val database by lazy {
        AppDatabase.getInstance(applicationContext)
    }
    val appPrefsRepository by lazy{
        AppUsageStatsRepository(database.getAppPrefsDao())
    }

//    val overlayDataRepository by lazy {
//        OverlayDataRepository(database.getOverlayDataDao())
//    }

    val quotesRepository by lazy {
        QuotesRepository(database.quotesDao())
    }

    val blogRepository by lazy {
        BlogRepository(database.blogDao())
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "Debug Build : ${BuildConfig.DEBUG}")
        //Logger.setLogLevel(LogLevel.OFF)

//        if (BuildConfig.DEBUG){
//            Logger.setLogLevel(LogLevel.DEBUG)
//        }else{
//            Logger.setLogLevel(LogLevel.OFF)
//        }



    }

}