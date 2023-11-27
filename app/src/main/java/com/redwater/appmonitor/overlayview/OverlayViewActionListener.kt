package com.redwater.appmonitor.overlayview

interface OverlayViewActionListener {

    fun onDismissOverlayAction(delayInMin: Short = 0)

    fun onOpenAppAction()

    fun onRendered(success: Boolean)
}