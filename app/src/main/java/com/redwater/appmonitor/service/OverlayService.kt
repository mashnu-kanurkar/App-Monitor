package com.redwater.appmonitor.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.ui.components.OverlayView
import com.redwater.appmonitor.ui.components.OverlayViewLayoutParams
import com.redwater.appmonitor.ui.components.UserDecisionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.Exception

class OverlayService : Service() {
    companion object{
        var isRunning = false
        private const val NOTIFICATION_ID_APP_MONITOR_SERVICE: Int = 1
    }
    private val TAG = "OverlayService"
    private var windowManager: WindowManager? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var overlayView: OverlayView? = null
    private var isOverlayShowing = false
    private val mainScope = MainScope()
    private lateinit var serviceScope: CoroutineScope
    private lateinit var overlayEventScope: CoroutineScope
    private var lastForegroundPackage = ""

    private var screenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    Logger.d("$TAG=> Screen ON")
                    observeForegroundApplication()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Logger.d("$TAG=> Screen OFF")
                    stopForegroundApplicationObserver()
                }
            }
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        Logger.d("$TAG => Service bind")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d("$TAG => Service created")
        initializeAppMonitorNotification()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d("$TAG => Starting service with command")
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
        Logger.d("$TAG => launching coroutine for tick handler")
        serviceScope = CoroutineScope(Dispatchers.Default)
        serviceScope.launch {
            val prefsRepo = (applicationContext as AppMonitorApp).appPrefsRepository
            //savedPppPrefsMap = prefsRepo.getAllRecords().toAppTimeMap()
            AppForegroundCheckTickHandler(this, 1000).tickFlow.collect{
                val foregroundApp = AppObserver.getInstance().getForegroundApp(applicationContext)
                Logger.d("$TAG => Received app event: $foregroundApp")
                foregroundApp?.let {
                    onAppForeground(it, prefsRepo)
                }
            }
        }
    }
    private suspend fun onAppForeground(foregroundAppPackage: String, prefsRepository: AppUsageStatsRepository) {
        Logger.d("$TAG => onAppForeground $foregroundAppPackage")
        if (foregroundAppPackage != lastForegroundPackage){
            //try to hide previous overlay, if any
            hideOverlay()
            lastForegroundPackage = foregroundAppPackage
            val savedPppPrefsMap: Map<String, Short> = prefsRepository.getAllRecords().toAppTimeMap()
            Logger.d("$TAG => saved app keys: ${savedPppPrefsMap.keys}")
            Logger.d("$TAG => saved app values: ${savedPppPrefsMap.values}")
            if (savedPppPrefsMap.containsKey(foregroundAppPackage)) {
                val currentAppAndUsageTimeInMin = prefsRepository.getAppUsageStatsFor(context = applicationContext, packageName = foregroundAppPackage)
                val savedTimeLimit = savedPppPrefsMap[currentAppAndUsageTimeInMin?.packageName] ?:(24*60).toShort()
                Logger.d("$TAG => threshold time  $savedTimeLimit vs app usage time  ${currentAppAndUsageTimeInMin?.time}")
                if (((currentAppAndUsageTimeInMin?.time) ?: 0) >= savedTimeLimit){
                    currentAppAndUsageTimeInMin?.packageName?.let { showOverlay(it) }
                }
            }
        }
    }

    private fun stopForegroundApplicationObserver() {
        Logger.d("$TAG => canceling service scope")
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

    private fun observeUserDecision(overlayView: OverlayView){
        Logger.d("$TAG => isOverlayShowing -> $isOverlayShowing")
        if (isOverlayShowing){
            overlayEventScope = CoroutineScope(Dispatchers.Default)
            overlayEventScope.launch {
                Logger.d("$TAG => Collecting button clicks")
                overlayView.userDecisionEvent.collect{userDecisionEvent->
                    Logger.d("$TAG => userDecisionEvent event: $userDecisionEvent")
                    when(userDecisionEvent){
                        is UserDecisionEvent.Forward ->{
                            hideOverlay()
                            cancel()
                        }
                        is UserDecisionEvent.Backward ->{
                            goToHome()
                            cancel()
                        }
                    }
                }
            }
        }
    }
    private fun goToHome(){
        hideOverlay()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun hideOverlay() {
        Logger.d("$TAG => hiding overlay ")
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
    private fun showOverlay(foregroundAppPackageName: String) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayParams = OverlayViewLayoutParams.get()
        overlayView = OverlayView(context = applicationContext)
        mainScope.launch {
            Logger.d("$TAG => Showing overlay")
            if (isOverlayShowing.not()) {
                isOverlayShowing = true
                overlayView?.setAppPackageName(foregroundAppPackageName)
                windowManager?.addView(overlayView, overlayParams)
                overlayView?.let {
                    observeUserDecision(it)
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
}

fun List<AppModel>.toAppTimeMap(): Map<String, Short>{
    val map = hashMapOf<String, Short>()
    forEach {
        map[it.packageName] = it.thresholdTime
    }
    return map
}
