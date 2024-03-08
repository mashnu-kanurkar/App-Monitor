package com.redwater.appmonitor.overlayview

import android.app.Service
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.data.model.AppDataFromSystem
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.ui.BasicTimeoutView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InstantOverlay internal constructor(private val applicationContext: Context,
                                          private var currentAppAndUsageTimeInMin: AppDataFromSystem,
                                          private var savedTimeLimit: Short = -1,
                                          private var withDelayInMin: Short = 0): IOverlayHandler {

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
//        this.currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin
//        this.savedTimeLimit = savedTimeLimit
        mainScope.launch {
            Logger.d(TAG, "Showing instant overlay")
            val isDelayOptionAvailable = withDelayInMin <= 0
            val limitString = if (withDelayInMin > 0) "$savedTimeLimit + $withDelayInMin" else "$savedTimeLimit"
            val basicTimeoutViewData = "{\"limit\":\"$limitString\",\"usage\":\"${currentAppAndUsageTimeInMin.usageTime}\",\"isDelayOptionAvbl\":\"$isDelayOptionAvailable\"}"
//            val overlayDataRepository = (applicationContext as AppMonitorApp).overlayDataRepository
//            var overlayPayload = overlayDataRepository.getRandomOverlayPayload()
//            Logger.d(TAG, "overlayPayload: $overlayPayload")
//            if (overlayPayload == null){
//                overlayPayload = OverlayPayload(id = -1, type = "puzzle", subType = "text", data = DefaultMathPuzzle().generateQuestion(), difficultyLevel = 1, isUsed = false)
//            }
            val overlayPayload = OverlayPayload(id = -1, type = "basic", subType = "front", data = basicTimeoutViewData, difficultyLevel = 1, isUsed = false)
            overlayView = OverlayUIProvider(context = applicationContext, overlayPayload = overlayPayload).getView()
            overlayView?.setOverlayActionListener(this@InstantOverlay)
            if (overlayPayload.type == "basic"){
                Logger.d(TAG, "Usage dist: ${currentAppAndUsageTimeInMin.usageDist}")
                currentAppAndUsageTimeInMin.usageDist?.let {
                    Logger.d(TAG, "Usage dist keys: ${it.keys}")
                    Logger.d(TAG, "Usage dist values: ${it.values}")
                    (overlayView as BasicTimeoutView).plotChart(
                        it, savedTimeLimit
                    )
                }
            }
            if (isOverlayShowing.not()) {
                isOverlayShowing = true
                windowManager?.addView(overlayView, overlayParams)
                overlayView?.setPackageIcon(currentAppAndUsageTimeInMin.packageName)
                overlayView?.let {
                    //observeUserDecision(it)
                }
            }
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
            Logger.d(TAG, "Delay over, will proceed for instant overlay")
            if (isUserInitiatedDelay) {
                showOverlay()
            } else {
                showOverlay()
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
        Logger.d(TAG, "hiding instant overlay")
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

    override fun onDismissOverlayAction(delayInMin: Short) {
        Logger.d(TAG, "Instant onPositiveAction")
        if (delayInMin > 0){
            delayedOverlayTask(delayInMin = delayInMin.toLong(),
                isUserInitiatedDelay = true)
            mainScope.launch {
                (applicationContext as AppMonitorApp).appPrefsRepository.addDelay(currentAppAndUsageTimeInMin.packageName, delayInMin)
            }
            hideOverlay()
        }else{
            goToHome()
        }
    }

    override fun onOpenAppAction() {
        Logger.d(TAG, "Instant onNegativeAction")
        hideOverlay()
    }

    override fun onRendered(success: Boolean) {
        Logger.d(TAG, "Instant overlay rendered successfully")
//                    if (overlayPayload.id != -1 && success) {
//                        mainScope.launch {
//                            markAsUsed(overlayPayload.id)
//                        }
//                    }
    }
}