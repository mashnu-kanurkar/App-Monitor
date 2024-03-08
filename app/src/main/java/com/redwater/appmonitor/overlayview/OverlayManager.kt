package com.redwater.appmonitor.overlayview

import android.content.Context
import com.redwater.appmonitor.data.model.AppDataFromSystem
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.utils.toAppTimeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverlayManager(val applicationContext: Context) {
    private val TAG = this::class.simpleName
    private lateinit var foregroundTimeMonitorScope: CoroutineScope
    private val overlayHandler: IOverlayHandler = InstantOverlay(applicationContext)
    suspend fun onAppForeground(foregroundAppPackage: String,
                                        prefsRepository: AppUsageStatsRepository,
                                        lastForegroundPackage: String,
                                        lastBackgroundPackage: String): String{
        Logger.d(TAG, "onAppForeground $foregroundAppPackage")

        if (foregroundAppPackage != lastForegroundPackage){
            if (lastBackgroundPackage != foregroundAppPackage){
                Logger.d(TAG, "trying to cancel foregroundTimeMonitor scope")
                terminateForegroundTimeMonitor()
            }
            //try to hide previous overlay, if any
            overlayHandler.hideOverlay()
            //lastForegroundPackage = foregroundAppPackage
            val savedAppPrefsMap: Map<String, AppModel> = prefsRepository.getAllSelectedRecords().toAppTimeMap()
            Logger.d(TAG, "saved app keys: ${savedAppPrefsMap.keys}")
            Logger.d(TAG, "saved app values: ${savedAppPrefsMap.values}")
            if (savedAppPrefsMap.containsKey(foregroundAppPackage)) {
                val currentAppAndUsageTimeInMin = prefsRepository.getAppUsageStatsFor(context = applicationContext, packageName = foregroundAppPackage)
                val savedTimeLimit = savedAppPrefsMap[currentAppAndUsageTimeInMin?.packageName]?.thresholdTime ?:(24*60).toShort()
                val userInitiatedDelay = savedAppPrefsMap[currentAppAndUsageTimeInMin?.packageName]?.delay ?: 0
                Logger.d(TAG, "threshold time  $savedTimeLimit + delay $userInitiatedDelay vs app usage time  ${currentAppAndUsageTimeInMin?.usageTime}")
                currentAppAndUsageTimeInMin?.let {
                    if (it.usageTime >= (savedTimeLimit + userInitiatedDelay)){
                        overlayHandler.showOverlay(it, savedTimeLimit, withDelayInMin = userInitiatedDelay)
                    }else{
                        deferOverlay(delayInMin = ((savedTimeLimit + userInitiatedDelay) - it.usageTime).toLong(), currentAppAndUsageTimeInMin = it, savedTimeLimit = savedTimeLimit, isUserInitiatedDelay = userInitiatedDelay > 0)
                    }
                }

            }
            return foregroundAppPackage
        }
        return lastForegroundPackage
    }

    private fun deferOverlay(delayInMin: Long,
                             currentAppAndUsageTimeInMin: AppDataFromSystem,
                             savedTimeLimit: Short,
                             isUserInitiatedDelay: Boolean = false){
        if (this::foregroundTimeMonitorScope.isInitialized.not()){
            foregroundTimeMonitorScope = CoroutineScope(Dispatchers.Default)
        }
        foregroundTimeMonitorScope.launch {
            Logger.d(TAG, "launched delayed task with delay of $delayInMin")
            delay((delayInMin*60*1000).toLong())
            Logger.d(TAG, "Delay over, will proceed for overlay")
            if (isUserInitiatedDelay)
                overlayHandler.showOverlay(currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit, withDelayInMin = delayInMin.toShort(), )
            else
                overlayHandler.showOverlay(currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit)
        }
    }

    private fun terminateForegroundTimeMonitor(){
        if (this::foregroundTimeMonitorScope.isInitialized){
            Logger.d(TAG, "canceling foregroundTimeMonitor scope")
            foregroundTimeMonitorScope.cancel()
        }
    }
}