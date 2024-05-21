package com.redwater.appmonitor.data.cache

import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.launch

class UsageStatsCacheManager private constructor() : ICacheManager<HashMap<String, AppModel>>() {

    private val TAG = this::class.simpleName
    companion object {
        @Volatile
        private var INSTANCE: ICacheManager<HashMap<String, AppModel>>? = null
        fun getInstance(): ICacheManager<HashMap<String, AppModel>> {
            return INSTANCE ?: synchronized(this) {
                val instance = UsageStatsCacheManager()
                INSTANCE = instance
                instance
            }
        }
    }

    private var usageStats: HashMap<String, AppModel>? = null
    override var lastSavedTs: Long = -1
    override var thresholdTime: Long = 5*60*1000
     override fun store(data: HashMap<String, AppModel>) {
         scope.launch {
             Logger.d(TAG, "caching usage stats ")
             try {
                 usageStats = data
                 lastSavedTs = System.currentTimeMillis()

             } catch (e: Exception){
                 e.printStackTrace()
                 Logger.e(TAG, "Unable to cache data." , e)
             }
         }
    }

    override fun getCacheData(): HashMap<String, AppModel>? {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSavedTs < thresholdTime && usageStats != null){
            return usageStats
        }
        flush()
        return null
    }

    override fun flush() {
        usageStats = null
    }


}