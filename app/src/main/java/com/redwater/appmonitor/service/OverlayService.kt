package com.redwater.appmonitor.service

import android.app.Service
import android.app.usage.UsageEvents
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.data.model.AppDataFromSystem
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.BaseOverlayView
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.overlayview.OverlayViewActionListener
import com.redwater.appmonitor.overlayview.OverlayViewProvider
import com.redwater.appmonitor.overlayview.OverlayViewLayoutParams
import com.redwater.appmonitor.overlayview.ui.BasicTimeoutView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverlayService : Service() {
    companion object{
        var isRunning = mutableStateOf(false)

        private const val NOTIFICATION_ID_APP_MONITOR_SERVICE: Int = 1
    }
    private val TAG = this::class.simpleName
    private var windowManager: WindowManager? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var overlayView: BaseOverlayView? = null
    private var isOverlayShowing = false
    private val mainScope = MainScope()
    private lateinit var serviceScope: CoroutineScope
    private lateinit var foregroundTimeMonitorScope: CoroutineScope
    private var lastForegroundPackage = ""
    private var lastBackgroundPackage = ""

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
        isRunning.value = true
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
                val appEvent = AppObserver.getInstance().getForegroundApp(applicationContext)
                Logger.d(TAG, "Received app event: ${appEvent?.event} and ${appEvent?.packageName}")
                appEvent?.let {
                    when(it.event){
                        UsageEvents.Event.ACTIVITY_RESUMED ->{
                            onAppForeground(foregroundAppPackage = it.packageName, prefsRepository = prefsRepo)
                        }
                        UsageEvents.Event.ACTIVITY_STOPPED ->{
                            onAppOnBackground(backgroundAppPackage= it.packageName)
                        }
                    }
                }
            }
        }
    }

    private fun onAppOnBackground(backgroundAppPackage: String){
        Logger.d(TAG, "updating last background app")
        lastBackgroundPackage = backgroundAppPackage
    }

    private fun terminateForegroundTimeMonitor(){
        if (this::foregroundTimeMonitorScope.isInitialized){
            Logger.d(TAG, "canceling foregroundTimeMonitor scope")
            foregroundTimeMonitorScope.cancel()
        }
    }
    private suspend fun onAppForeground(foregroundAppPackage: String, prefsRepository: AppUsageStatsRepository) {
        Logger.d(TAG, "onAppForeground $foregroundAppPackage")

        if (foregroundAppPackage != lastForegroundPackage){
            if (lastBackgroundPackage != foregroundAppPackage){
                Logger.d(TAG, "trying to cancel foregroundTimeMonitor scope")
                terminateForegroundTimeMonitor()
            }
            //try to hide previous overlay, if any
            hideOverlay()
            lastForegroundPackage = foregroundAppPackage
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
                        showOverlay(it, savedTimeLimit, withDelayInMin = userInitiatedDelay)
                    }else{
                        delayedOverlayTask(delayInMin = ((savedTimeLimit + userInitiatedDelay) - it.usageTime).toLong(), currentAppAndUsageTimeInMin = it, savedTimeLimit = savedTimeLimit, isUserInitiatedDelay = userInitiatedDelay > 0)
                    }
                }

            }
        }
    }

    private fun delayedOverlayTask(delayInMin: Long, currentAppAndUsageTimeInMin: AppDataFromSystem, savedTimeLimit: Short, isUserInitiatedDelay: Boolean = false){
        if (this::foregroundTimeMonitorScope.isInitialized.not()){
            foregroundTimeMonitorScope = CoroutineScope(Dispatchers.Default)
        }
        foregroundTimeMonitorScope.launch {
            Logger.d(TAG, "launched delayed task with delay of $delayInMin")
            delay(delayInMin*60*1000)
            Logger.d(TAG, "Delay over, will proceed for overlay")
            if (isUserInitiatedDelay)
                showOverlay(currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit, withDelayInMin = delayInMin.toShort(), )
            else
                showOverlay(currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit)
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
                overlayParams = null
                overlayView = null
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }
    private fun showOverlay(currentAppAndUsageTimeInMin: AppDataFromSystem, savedTimeLimit: Short, withDelayInMin: Short = 0) {
        Logger.d(TAG, "Initiating overlay process")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayParams = OverlayViewLayoutParams.get()
        mainScope.launch {
            Logger.d(TAG, "Showing overlay")
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
            overlayView = OverlayViewProvider(context = applicationContext, overlayPayload = overlayPayload).getView()
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
            (overlayView as BaseOverlayView).setOverlayActionListener(object : OverlayViewActionListener{
                override fun onDismissOverlayAction(delayInMin: Short) {
                    Logger.d(TAG, "onPositiveAction")
                    if (delayInMin > 0){
                        delayedOverlayTask(delayInMin = delayInMin.toLong(), currentAppAndUsageTimeInMin = currentAppAndUsageTimeInMin, savedTimeLimit = savedTimeLimit, isUserInitiatedDelay = true)
                        mainScope.launch {
                            (applicationContext as AppMonitorApp).appPrefsRepository.addDelay(currentAppAndUsageTimeInMin.packageName, delayInMin)
                        }
                        hideOverlay()
                    }else{
                        goToHome()
                    }

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
            isRunning.value = false
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

fun List<AppModel>.toAppTimeMap(): Map<String, AppModel>{
    val map = hashMapOf<String, AppModel>()
    forEach {
        map[it.packageName] = it
    }
    return map
}
