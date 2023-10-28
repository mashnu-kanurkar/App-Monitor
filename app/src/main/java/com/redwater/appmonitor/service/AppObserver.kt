package com.redwater.appmonitor.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.redwater.appmonitor.logger.Logger

class AppObserver {
    companion object{
        private var INSTANCE: AppObserver? = null
        private const val TAG = "AppObserver"

        fun getInstance(): AppObserver {
            return INSTANCE ?: synchronized(this){
                val instance = AppObserver()
                INSTANCE = instance
                instance
            }
        }
    }

    fun getForegroundApp(context: Context): String?{
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(time - 1000*5, time)
        val event: UsageEvents.Event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                Logger.d("$TAG => timestamp is ${event.timeStamp}")
               return event.packageName
            }
        }
        return null
    }
}