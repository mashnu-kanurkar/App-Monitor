package com.redwater.appmonitor.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.redwater.appmonitor.data.model.AppEvent
import com.redwater.appmonitor.logger.Logger

class AppObserver {
    private val TAG = this::class.simpleName
    companion object{
        private var INSTANCE: AppObserver? = null

        fun getInstance(): AppObserver {
            return INSTANCE ?: synchronized(this){
                val instance = AppObserver()
                INSTANCE = instance
                instance
            }
        }
    }

    fun getForegroundApp(context: Context): AppEvent?{
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(time - 1000*5, time)
        val event: UsageEvents.Event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                Logger.d(TAG, "timestamp is ${event.timeStamp}")
               return AppEvent(packageName = event.packageName, event = event.eventType)
            }else if(event.eventType == UsageEvents.Event.ACTIVITY_STOPPED){
                Logger.d(TAG, "timestamp is ${event.timeStamp}")
                return AppEvent(packageName = event.packageName, event = event.eventType)
            }
        }
        return null
    }
}