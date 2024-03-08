package com.redwater.appmonitor.overlayview

import android.content.Context
import com.redwater.appmonitor.data.model.AppDataFromSystem

class OverlayProvider(private val applicationContext: Context){

    private val TAG = this::class.java.simpleName
    private var currentOverLay: IOverlayHandler? = null
    fun buildOverlayUI(currentAppAndUsageTimeInMin: AppDataFromSystem, savedTimeLimit: Short = -1, withDelayInMin: Short = 0): IOverlayHandler? {
        currentOverLay = null
        currentOverLay = InstantOverlay(applicationContext = applicationContext, currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit, withDelayInMin = withDelayInMin)
        return currentOverLay
    }

    fun buildOverlayUI(packageName: String, dndStartTime: String, dndEndTime: String): IOverlayHandler? {
        currentOverLay = null
        currentOverLay = DNDOverlay(applicationContext = applicationContext, packageName = packageName, dndStartTime = dndStartTime, dndEndTime = dndEndTime)
        return currentOverLay
    }
}