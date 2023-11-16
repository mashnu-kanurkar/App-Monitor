package com.redwater.appmonitor.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.JsonWriter
import android.view.WindowManager
import androidx.activity.ComponentActivity
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.data.model.AppAndTime
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.BaseOverlayView
import com.redwater.appmonitor.overlayview.DefaultMathPuzzle
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.overlayview.OverlayViewActionListener
import com.redwater.appmonitor.overlayview.OverlayViewProvider
import com.redwater.appmonitor.overlayview.OverlayViewLayoutParams
import com.redwater.appmonitor.overlayview.ui.BasicTimeoutView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OverlayService : Service() {
    companion object{
        var isRunning = false
        private const val NOTIFICATION_ID_APP_MONITOR_SERVICE: Int = 1
    }
    private val TAG = this::class.simpleName
    private var windowManager: WindowManager? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var overlayView: BaseOverlayView? = null
    private var isOverlayShowing = false
    private val mainScope = MainScope()
    private lateinit var serviceScope: CoroutineScope
    private lateinit var overlayEventScope: CoroutineScope
    private var lastForegroundPackage = ""

    private var screenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    Logger.d(TAG, "Screen ON")
                    observeForegroundApplication()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Logger.d(TAG, "Screen OFF")
                    stopForegroundApplicationObserver()
                }
            }
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        Logger.d(TAG, "Service bind")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "Service created")
        initializeAppMonitorNotification()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "Starting service with command")
        isRunning = true
        observeForegroundApplication()
        return START_STICKY
    }

    private fun observeForegroundApplication() {
        //Whenever this function is invoked, cancel previous scope
        try {
            if (this::serviceScope.isInitialized){
                serviceScope.cancel()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        Logger.d(TAG, "launching coroutine for tick handler")
        serviceScope = CoroutineScope(Dispatchers.Default)
        serviceScope.launch {
            val prefsRepo = (applicationContext as AppMonitorApp).appPrefsRepository
            //savedPppPrefsMap = prefsRepo.getAllRecords().toAppTimeMap()
            AppForegroundCheckTickHandler(this, 1000).tickFlow.collect{
                val foregroundApp = AppObserver.getInstance().getForegroundApp(applicationContext)
                Logger.d(TAG, "Received app event: $foregroundApp")
                foregroundApp?.let {
                    onAppForeground(it, prefsRepo)
                }
            }
        }
    }
    private suspend fun onAppForeground(foregroundAppPackage: String, prefsRepository: AppUsageStatsRepository) {
        Logger.d(TAG, "onAppForeground $foregroundAppPackage")
        if (foregroundAppPackage != lastForegroundPackage){
            //try to hide previous overlay, if any
            hideOverlay()
            lastForegroundPackage = foregroundAppPackage
            val savedPppPrefsMap: Map<String, Short> = prefsRepository.getAllRecords().toAppTimeMap()
            Logger.d(TAG, "saved app keys: ${savedPppPrefsMap.keys}")
            Logger.d(TAG, "saved app values: ${savedPppPrefsMap.values}")
            if (savedPppPrefsMap.containsKey(foregroundAppPackage)) {
                val currentAppAndUsageTimeInMin = prefsRepository.getAppUsageStatsFor(context = applicationContext, packageName = foregroundAppPackage)
                val savedTimeLimit = savedPppPrefsMap[currentAppAndUsageTimeInMin?.packageName] ?:(24*60).toShort()
                Logger.d(TAG, "threshold time  $savedTimeLimit vs app usage time  ${currentAppAndUsageTimeInMin?.time}")
                if (((currentAppAndUsageTimeInMin?.time) ?: 0) >= savedTimeLimit){
                    if (currentAppAndUsageTimeInMin != null) {
                        showOverlay(currentAppAndUsageTimeInMin, savedTimeLimit)
                    }
                    //currentAppAndUsageTimeInMin?.packageName?.let { showOverlay(it) }
                }
            }
        }
    }

    private fun stopForegroundApplicationObserver() {
        Logger.d(TAG, "canceling service scope")
        try {
            if (this::serviceScope.isInitialized){
                serviceScope.cancel()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun initializeAppMonitorNotification() {
        val serviceNotificationManager = ServiceNotificationManager(applicationContext)
        val notification = serviceNotificationManager.createNotification()
        val permissionManager = PermissionManager()
        if (permissionManager.hasNotificationPermission(applicationContext)){
            startForeground(NOTIFICATION_ID_APP_MONITOR_SERVICE, notification)
        }else{
            permissionManager.requestPermission((applicationContext as ComponentActivity)){
                if (it) startForeground(NOTIFICATION_ID_APP_MONITOR_SERVICE, notification)
            }
        }
    }

    private fun registerScreenReceiver() {
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOnOffReceiver, screenFilter)
    }

    private fun unregisterScreenReceiver() {
        unregisterReceiver(screenOnOffReceiver)
    }
    private fun goToHome(){
        hideOverlay()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun hideOverlay() {
        Logger.d(TAG, "hiding overlay")
        try {
            mainScope.launch {
                if (isOverlayShowing && overlayView != null) {
                    isOverlayShowing = false
                    windowManager?.removeViewImmediate(overlayView)
                }
                if (this@OverlayService::overlayEventScope.isInitialized){
                    overlayEventScope.cancel()
                }
                overlayParams = null
                overlayView = null
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }
    private fun showOverlay(currentAppAndUsageTimeInMin: AppAndTime, savedTimeLimit: Short) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayParams = OverlayViewLayoutParams.get()
        mainScope.launch {
            Logger.d(TAG, "Showing overlay")
            val basicTimeoutViewData = "{\"limit\":\"$savedTimeLimit\",\"usage\":\"${currentAppAndUsageTimeInMin.time}\"}"
//            val overlayDataRepository = (applicationContext as AppMonitorApp).overlayDataRepository
//            var overlayPayload = overlayDataRepository.getRandomOverlayPayload()
//            Logger.d(TAG, "overlayPayload: $overlayPayload")
//            if (overlayPayload == null){
//                overlayPayload = OverlayPayload(id = -1, type = "puzzle", subType = "text", data = DefaultMathPuzzle().generateQuestion(), difficultyLevel = 1, isUsed = false)
//            }
            val overlayPayload = OverlayPayload(id = -1, type = "basic", subType = "front", data = basicTimeoutViewData, difficultyLevel = 1, isUsed = false)
            overlayView = OverlayViewProvider(context = applicationContext, overlayPayload = overlayPayload).getView()
            if (overlayPayload.type == "basic"){
                Logger.d(TAG, "Usage dist: ${currentAppAndUsageTimeInMin.usageDist}")
                currentAppAndUsageTimeInMin.usageDist?.let {
                    Logger.d(TAG, "Usage dist keys: ${it.keys}")
                    Logger.d(TAG, "Usage dist values: ${it.values}")
                    (overlayView as BasicTimeoutView).plotChart(
                        it
                    )
                }
            }
            (overlayView as BaseOverlayView).setOverlayActionListener(object : OverlayViewActionListener{
                override fun onCloseAppAction(delayInMin: Short) {
                    Logger.d(TAG, "onPositiveAction")
                    goToHome()
                }

                override fun onOpenAppAction() {
                    Logger.d(TAG, "onNegativeAction")
                    hideOverlay()
                }

                override fun onRendered(success: Boolean) {
                    Logger.d(TAG, "overlay rendered successfully")
                    if (overlayPayload.id != -1 && success) {
                        mainScope.launch {
                            markAsUsed(overlayPayload.id)
                        }
                    }
                }

            })
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

    override fun onDestroy() {
        try {
            unregisterScreenReceiver()
            stopForegroundApplicationObserver()
            isRunning = false
            if (overlayView?.isAttachedToWindow == true){
                windowManager?.removeView(overlayView)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private suspend fun markAsUsed(id: Int){
        (applicationContext as AppMonitorApp).overlayDataRepository.markAsUSed(id)
    }
}

fun List<AppModel>.toAppTimeMap(): Map<String, Short>{
    val map = hashMapOf<String, Short>()
    forEach {
        map[it.packageName] = it.thresholdTime
    }
    return map
}
