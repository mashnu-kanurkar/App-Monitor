package com.redwater.appmonitor.overlayview

import android.content.Context
import com.redwater.appmonitor.data.model.AppDataFromSystem
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.utils.TimeFormatUtility
import com.redwater.appmonitor.utils.toAppTimeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class OverlayManager(val applicationContext: Context) {
    private val TAG = this::class.simpleName
    private lateinit var foregroundTimeMonitorScope: CoroutineScope
    private val overlayProvider: OverlayProvider = OverlayProvider(applicationContext)
    private var lastForegroundPackage = ""
    suspend fun onAppForeground(foregroundAppPackage: String,
                                        prefsRepository: AppUsageStatsRepository,
                                        lastBackgroundPackage: String): String{
        Logger.d(TAG, "onAppForeground $foregroundAppPackage")
        if (foregroundAppPackage != lastForegroundPackage){
            lastForegroundPackage = foregroundAppPackage
            if (lastBackgroundPackage != foregroundAppPackage){
                Logger.d(TAG, "trying to cancel foregroundTimeMonitor scope")
                terminateForegroundTimeMonitor()
            }
            //try to hide previous overlay, if any
            val savedAppPrefsMap: Map<String, AppModel> = prefsRepository.getAllSelectedRecords().toAppTimeMap()
            Logger.d(TAG, "saved app keys: ${savedAppPrefsMap.keys}")
            Logger.d(TAG, "saved app values: ${savedAppPrefsMap.values}")
            if (savedAppPrefsMap.containsKey(foregroundAppPackage)) {
                if (savedAppPrefsMap[foregroundAppPackage]!!.dndStartTime != null &&
                    savedAppPrefsMap[foregroundAppPackage]!!.dndEndTime != null) {
                    val currentTime = System.currentTimeMillis()
                    val dndStart = savedAppPrefsMap[foregroundAppPackage]!!.dndStartTime!!
                    val dndEnd = savedAppPrefsMap[foregroundAppPackage]!!.dndEndTime!!

                    val dateFormat = SimpleDateFormat("hh:mm a", Locale.US)
                    val timeToCheck = dateFormat.format(currentTime)
                    if (TimeFormatUtility().isWithinDNDTime(timeToCheckStr = timeToCheck, startTimeStr = dndStart, endTimeStr = dndEnd)){
                        overlayProvider.buildOverlayUI(packageName = foregroundAppPackage, dndStartTime = dndStart, dndEndTime = dndEnd)?.showOverlay()
                    }else{
                        val upcomingDND = TimeFormatUtility().upcomingDNDDelay(timeToCheckStr = timeToCheck, startTimeStr = dndStart)
                        if (upcomingDND <= 60){
                            overlayProvider.buildOverlayUI(packageName = foregroundAppPackage, dndStartTime = dndStart, dndEndTime = dndEnd)
                                ?.delayedOverlayTask(delayInMin = upcomingDND, isUserInitiatedDelay = false)
                        }
                    }
                    return foregroundAppPackage
                }
                val currentAppAndUsageTimeInMin = prefsRepository.getAppUsageStatsFor(context = applicationContext,
                    packageName = foregroundAppPackage)
                val savedTimeLimit = savedAppPrefsMap[currentAppAndUsageTimeInMin?.packageName]?.thresholdTime ?:(24*60).toShort()
                val userInitiatedDelay = savedAppPrefsMap[currentAppAndUsageTimeInMin?.packageName]?.delay ?: 0
                Logger.d(TAG, "threshold time  $savedTimeLimit + delay $userInitiatedDelay vs app usage time  ${currentAppAndUsageTimeInMin?.usageTime}")
                currentAppAndUsageTimeInMin?.let {
                    if (it.usageTime >= (savedTimeLimit + userInitiatedDelay)){
                        overlayProvider.buildOverlayUI(it, savedTimeLimit, withDelayInMin = userInitiatedDelay)?.showOverlay()
                    }else{
                        overlayProvider.buildOverlayUI(it, savedTimeLimit, withDelayInMin = userInitiatedDelay)
                            ?.delayedOverlayTask(delayInMin = ((savedTimeLimit + userInitiatedDelay) - it.usageTime).toLong(),isUserInitiatedDelay = userInitiatedDelay > 0)
                        //deferOverlay(delayInMin = ((savedTimeLimit + userInitiatedDelay) - it.usageTime).toLong(), currentAppAndUsageTimeInMin = it, savedTimeLimit = savedTimeLimit, isUserInitiatedDelay = userInitiatedDelay > 0)
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
            delay((delayInMin*60*1000))
            Logger.d(TAG, "Delay over, will proceed for overlay")
            if (isUserInitiatedDelay)
                overlayProvider.buildOverlayUI(currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit, withDelayInMin = delayInMin.toShort(), )?.showOverlay()
            else
                overlayProvider.buildOverlayUI(currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit)?.showOverlay()
        }
    }

    private fun terminateForegroundTimeMonitor(){
        if (this::foregroundTimeMonitorScope.isInitialized){
            Logger.d(TAG, "canceling foregroundTimeMonitor scope")
            foregroundTimeMonitorScope.cancel()
        }
    }
}