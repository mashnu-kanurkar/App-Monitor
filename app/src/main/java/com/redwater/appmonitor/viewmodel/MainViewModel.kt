package com.redwater.appmonitor.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.service.OverlayService
import com.redwater.appmonitor.service.ServiceManager
import com.redwater.appmonitor.ui.NotificationPermission
import com.redwater.appmonitor.ui.OverlayPermission
import com.redwater.appmonitor.ui.PermissionState
import com.redwater.appmonitor.ui.PermissionType
import com.redwater.appmonitor.ui.UsagePermission
import com.redwater.appmonitor.workmanager.FirebaseSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel(private val preferenceRepository: AppUsageStatsRepository,): ViewModel() {

    var uiState = mutableStateListOf<AppModel>()
        private set
    var uiStateSelected = mutableStateListOf<AppModel>()
        private set
    var isLoadingData = mutableStateOf(false)
        private set
    var popUp = mutableStateOf<PopUp>(PopUp.Hide)
        private set
    var permissionStateMap = mutableStateMapOf<Int, PermissionState>()
    private val permissionManager = PermissionManager()
    private val TAG = this::class.simpleName

    var isServiceRunning = mutableStateOf(OverlayService.isRunning)
        private set

    init {
        isServiceRunning.value = OverlayService.isRunning
    }

    private fun startService(context: Context){
        if (OverlayService.isRunning.not()){
            viewModelScope.launch {
                ServiceManager.startService(context = context)
                //isServiceRunning.value = true
            }
        }
    }

    private fun stopService(context: Context){
        if (OverlayService.isRunning){
            ServiceManager.stopService(context = context)
            //isServiceRunning.value = false
        }
    }

    fun getPermissionState(context: Context){
        Logger.d(TAG, "getting permission")
        val usagePermission = permissionManager.hasUsagePermission(context)
        val overlayPermission = permissionManager.hasOverlayPermission(context)
        var notificationPermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            notificationPermission = permissionManager.hasNotificationPermission(context)
        }
        permissionStateMap.putAll(mapOf(PermissionType.usagePermission to UsagePermission(context, usagePermission), PermissionType.overlayPermission to OverlayPermission(context, overlayPermission), PermissionType.notificationPermission to NotificationPermission(context, notificationPermission)))
    }

    fun onPopUpClick(type: Int, isPositive: Boolean, context: Context){
        when(type){
            PermissionType.usagePermission ->{
                if (isPositive && permissionManager.hasUsagePermission(context).not())
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            PermissionType.overlayPermission ->{
                if (isPositive && permissionManager.hasOverlayPermission(context).not())
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
            }
            PermissionType.notificationPermission ->{
                Logger.d(TAG, "type: $type isPositive: $isPositive ")
                permissionStateMap[PermissionType.notificationPermission]?.hasPermission = isPositive
            }
        }
    }

    fun getAppUsageTime(context: Context){
        Logger.d(TAG, "getting app usage")
        if (permissionManager.hasUsagePermission(context).not()){
            popUp.value = PopUp.Show(PermissionType.usagePermission)
            return
        }
        isLoadingData.value = true
        viewModelScope.launch {
            withContext(Dispatchers.Default){
                try{
                    val allApps = preferenceRepository.getAllAvailableApps(context = context)
                    val allAppUsageMap = preferenceRepository.getAllAppUsageStats(context, allApps)
                    val savedAppList = preferenceRepository.getAllRecords()
                    savedAppList.forEach {appModel->
                        if (allAppUsageMap.containsKey(appModel.packageName)){
                            allAppUsageMap[appModel.packageName]?.let {appUsageStats->
                                allAppUsageMap[appModel.packageName] = appUsageStats.copy(isSelected = true, thresholdTime = appModel.thresholdTime)
                            }
                        }
                    }
                    allAppUsageMap.forEach { (key, value) ->
                        Logger.d(TAG, "$key: $value")
                        if (value.isSelected){
                            uiStateSelected.add(value)
                        }else{
                            uiState.add(value)
                        }
                    }
                    uiStateSelected.sortWith(compareBy { it.usageTime })
                    uiState.sortWith(compareBy { it.name })

                }catch (e: Exception){
                    e.printStackTrace()
                }
                isLoadingData.value = false
            }
        }
    }

    //when unselected app is selected
    fun onAppSelected(index: Int, thresholdTimeInString: String, context: Context){
        viewModelScope.launch {
            var thresholdTimeInMin: Short = Short.MAX_VALUE
            try {
                val timeSplit = thresholdTimeInString.split(" ")
                if (timeSplit[1].contains("Hr")){
                    thresholdTimeInMin = (timeSplit[0].toShort() * 60).toShort()
                }else{
                    thresholdTimeInMin = timeSplit[0].toShort()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            val appInfo =  uiState[index].copy(isSelected = true, thresholdTime = thresholdTimeInMin)
            uiState.removeAt(index)
            uiStateSelected.add(appInfo)
            preferenceRepository.insertPrefsFor(AppModel(packageName = appInfo.packageName,
                name = appInfo.name,
                isSelected = true,
                thresholdTime = thresholdTimeInMin.toShort()))
        }
        if (uiStateSelected.size >= 1){
            startService(context = context.applicationContext)
        }
    }

    //when selected app is unselected
    fun onAppUnSelected(index: Int, context: Context){
        val appInfo =  uiStateSelected[index].copy(isSelected = false)
        uiStateSelected.removeAt(index)
        uiState.add(appInfo)
        viewModelScope.launch {
            preferenceRepository.deletePrefsFor(appInfo.packageName)
        }
        uiState.sortWith(compareByDescending { it.usageTime })
        if (uiStateSelected.size <= 0){
            stopService(context = context)
        }
    }
}

sealed class PopUp{
    class Show(val type: Int): PopUp()
    object Hide: PopUp()
}