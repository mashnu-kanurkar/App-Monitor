package com.redwater.appmonitor

import android.app.Application
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.redwater.appmonitor.data.AppDatabase
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.BlogRepository
import com.redwater.appmonitor.data.repository.QuotesRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.logger.LogLevel

class AppMonitorApp: Application(){

    private val TAG = this::class.simpleName

    private val database by lazy {
        AppDatabase.getInstance(applicationContext)
    }
    val appPrefsRepository by lazy{
        AppUsageStatsRepository(database.getAppPrefsDao())
    }

    val quotesRepository by lazy {
        QuotesRepository(database.quotesDao())
    }

    val blogRepository by lazy {
        BlogRepository(database.blogDao())
    }

    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Logger.setLogLevel(LogLevel.VERBOSE)
        }else{
            Logger.setLogLevel(LogLevel.OFF)
        }

    }

}