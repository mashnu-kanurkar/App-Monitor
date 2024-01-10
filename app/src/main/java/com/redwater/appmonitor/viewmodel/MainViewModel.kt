package com.redwater.appmonitor.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.service.OverlayService
import com.redwater.appmonitor.service.ServiceManager
import com.redwater.appmonitor.ui.PermissionState
import com.redwater.appmonitor.ui.PermissionType
import com.redwater.appmonitor.utils.TimeFormatUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel(private val preferenceRepository: AppUsageStatsRepository,): ViewModel() {

    var uiStateUnselected = mutableStateListOf<AppModel>()
        private set
    var uiStateSelected = mutableStateListOf<AppModel>()
        private set
    var isLoadingData = mutableStateOf(false)
        private set
    var popUp = mutableStateOf<PopUp>(PopUp.Hide)
        private set
    var permissionStateMap = mutableStateMapOf<Int, PermissionState>()
        private set
    private val permissionManager = PermissionManager()
    private val TAG = this::class.simpleName

    var isServiceRunning = mutableStateOf(OverlayService.isRunning)
        private set

    private var allAppUsageMap = mutableMapOf<String, AppModel>()

    private fun startService(context: Context){
        if (OverlayService.isRunning.not()){
            viewModelScope.launch {
                ServiceManager.startService(context = context)
                //isServiceRunning.value = true
            }
        }
    }

    private fun stopService(context: Context){
        viewModelScope.launch {
            if (OverlayService.isRunning){
                ServiceManager.stopService(context = context, repository = preferenceRepository)
                //isServiceRunning.value = false
            }
        }
    }

    fun getPermissionState(context: Context){
        permissionStateMap.putAll(permissionManager.getPermissionState(context = context))
    }

    fun onPopUpClick(type: Int, isPositive: Boolean, context: Context){
        permissionManager.requestPermissionSystemUI(type = type, isPositive = isPositive, context = context)
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
                    allAppUsageMap = preferenceRepository.getAppModelData(context = context)
                    //This works only for newly added entries. In case we delete any entry, we need to manually remove them at onAppUnSelected()
                    preferenceRepository.getAllRecordsFlow().collectLatest { savedAppList->
                        Logger.d(TAG, "collected app list $savedAppList")
                        savedAppList.forEach {appModel->
                            if (allAppUsageMap.containsKey(appModel.packageName)){
                                allAppUsageMap[appModel.packageName]?.let {appUsageStats->
                                    allAppUsageMap[appModel.packageName] = appUsageStats.copy(isSelected = appModel.isSelected, thresholdTime = appModel.thresholdTime)
                                }
                            }
                        }
                        Logger.d(TAG, "QA-1: allMap ${allAppUsageMap.entries}")
                        val partition = allAppUsageMap.values.partition { it.isSelected }
                        Logger.d(TAG, "QA-1: partition1 ${partition.first.toList()}")
                        Logger.d(TAG, "QA-1: partition2 ${partition.second.toList()}")
                        Logger.d(TAG, "QA-1: selected ${uiStateSelected.toList()}")
                        Logger.d(TAG, "QA-1: other ${uiStateUnselected.toList()}")
                        if (uiStateSelected.isEmpty().not()){
                            uiStateSelected.clear()
                            Logger.d(TAG, "QA-2: selected ${uiStateSelected.toList()}")
                        }
                        uiStateSelected.addAll(partition.first)

                        if (uiStateUnselected.isEmpty().not()){
                            uiStateUnselected.clear()
                            Logger.d(TAG, "QA-2: other ${uiStateUnselected.toList()}")
                        }

                        uiStateUnselected.addAll(partition.second)
                        Logger.d(TAG, "QA-3: selected ${uiStateSelected.toList()}")
                        Logger.d(TAG, "QA-3: other ${uiStateUnselected.toList()}")
                        uiStateSelected.sortWith(compareBy { it.usageTimeInMillis })
                        uiStateUnselected.sortWith(compareBy { it.name })
                        isLoadingData.value = false
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
                isLoadingData.value = false
            }
        }
    }

    //when unselected app is selected
    fun onAppSelected(index: Int, thresholdTimeInString: String){
        viewModelScope.launch {
            val thresholdTimeInMin: Short = TimeFormatUtility().getTimeInMin(thresholdTimeInString = thresholdTimeInString)
            val appInfo =  uiStateUnselected[index].copy(isSelected = true, thresholdTime = thresholdTimeInMin)
            preferenceRepository.insertPrefsFor(AppModel(packageName = appInfo.packageName,
                name = appInfo.name,
                isSelected = true,
                thresholdTime = thresholdTimeInMin.toShort()))
        }
    }

    //when selected app is unselected
    fun onAppUnSelected(index: Int){
        val appInfo =  uiStateSelected[index].copy(isSelected = false)
        allAppUsageMap.remove(appInfo.packageName)
//        uiStateSelected.removeAt(index)
//        uiStateUnselected.add(appInfo)
        viewModelScope.launch {
            preferenceRepository.unselectPrefsFor(appInfo.packageName)
        }
        uiStateUnselected.sortWith(compareByDescending { it.name })
    }
}

sealed class PopUp{
    class Show(val type: Int): PopUp()
    object Hide: PopUp()
}