package com.redwater.appmonitor.overlayview

import com.redwater.appmonitor.data.model.AppDataFromSystem

interface IOverlayHandler: IOverlayViewActionListener {

    fun showOverlay(currentAppAndUsageTimeInMin: AppDataFromSystem, savedTimeLimit: Short, withDelayInMin: Short = 0)

    fun hideOverlay()


}