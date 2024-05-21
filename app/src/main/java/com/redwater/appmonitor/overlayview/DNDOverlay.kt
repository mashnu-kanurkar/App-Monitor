package com.redwater.appmonitor.overlayview

import android.app.Service
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DNDOverlay internal constructor(private val applicationContext: Context,
                                      private val packageName: String,
    private val dndStartTime: String, private val dndEndTime: String): IOverlayHandler{
    private val TAG = this::class.simpleName
    private var windowManager: WindowManager? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var overlayView: BaseOverlayView? = null
    private var isOverlayShowing = false
    private val mainScope = MainScope()
    private lateinit var foregroundTimeMonitorScope: CoroutineScope

    override fun showOverlay(){
        windowManager = applicationContext.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        overlayParams = OverlayViewLayoutParams.get()
        mainScope.launch {
            Logger.d(TAG, "Showing DND overlay")
            val basicDNDData = "{\"dndStart\":\"$dndStartTime\",\"dndEnd\":\"$dndEndTime\"}"
            val overlayPayload = OverlayPayload(id = -1, type = "dnd", data = basicDNDData)
            overlayView = OverlayUIProvider(context = applicationContext, overlayPayload = overlayPayload).getView()
            overlayView?.setOverlayActionListener(this@DNDOverlay)
            if (isOverlayShowing.not()) {
                isOverlayShowing = true
                windowManager?.addView(overlayView, overlayParams)
                overlayView?.setPackageIcon(packageName)
            }
        }
    }
    private fun goToHome(){
        hideOverlay()
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(homeIntent)
    }

    override fun hideOverlay() {
        Logger.d(TAG, "hiding DND overlay")
        try {
            mainScope.launch {
                if (isOverlayShowing && overlayView != null) {
                    isOverlayShowing = false
                    windowManager?.removeViewImmediate(overlayView)
                }
                overlayParams = null
                overlayView = null
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }
    override fun delayedOverlayTask(delayInMin: Long,
                                    isUserInitiatedDelay: Boolean){
        if (this::foregroundTimeMonitorScope.isInitialized.not()){
            foregroundTimeMonitorScope = CoroutineScope(Dispatchers.Default)
        }
        foregroundTimeMonitorScope.launch {
            Logger.d(TAG, "launched delayed task with delay of $delayInMin")
            delay(delayInMin*60*1000)
            Logger.d(TAG, "Delay over, will proceed for DND overlay")
            if (isUserInitiatedDelay) {
                showOverlay()
            }
            else {
                showOverlay()
            }
        }
    }

    override fun terminateForegroundMonitorScope() {
        if (this::foregroundTimeMonitorScope.isInitialized){
            Logger.d(TAG, "canceling foregroundTimeMonitor scope")
            foregroundTimeMonitorScope.cancel()
        }
    }
    override fun onDismissOverlayAction(delayInMin: Short) {
        goToHome()
    }

    override fun onOpenAppAction() {
        Logger.d(TAG, "DND onNegativeAction")
        hideOverlay()
    }

    override fun onRendered(success: Boolean) {
        Logger.d(TAG, "DND overlay rendered successfully")
    }

}