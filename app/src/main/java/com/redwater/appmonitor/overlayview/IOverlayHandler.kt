package com.redwater.appmonitor.overlayview

interface IOverlayHandler: IOverlayViewActionListener {
    fun showOverlay()
    fun hideOverlay()
    fun delayedOverlayTask(delayInMin: Long, isUserInitiatedDelay: Boolean)

}