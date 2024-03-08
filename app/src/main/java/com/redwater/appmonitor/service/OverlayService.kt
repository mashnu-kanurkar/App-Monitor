package com.redwater.appmonitor.service

import android.app.Service
import android.app.usage.UsageEvents
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import com.redwater.appmonitor.AppMonitorApp
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.OverlayManager
import com.redwater.appmonitor.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OverlayService : Service() {
    companion object{
        var isRunning = mutableStateOf(false)

        private const val NOTIFICATION_ID_APP_MONITOR_SERVICE: Int = 1
    }
    private val TAG = this::class.simpleName
    private lateinit var serviceScope: CoroutineScope
    private lateinit var overlayManager: OverlayManager
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
        overlayManager = OverlayManager(applicationContext)
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
                Logger.d(TAG, "Received app event: ${appEvent?.event} for package name: ${appEvent?.packageName}")
                appEvent?.let {
                    when(it.event){
                        UsageEvents.Event.ACTIVITY_RESUMED ->{
                            lastBackgroundPackage = overlayManager.onAppForeground(
                                foregroundAppPackage = it.packageName,
                                prefsRepository = prefsRepo,
                                lastBackgroundPackage = lastBackgroundPackage)
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

    override fun onDestroy() {
        try {
            unregisterScreenReceiver()
            stopForegroundApplicationObserver()
            isRunning.value = false
        }catch (e: Exception){
            e.printStackTrace()
        }
        super.onDestroy()
    }

}
