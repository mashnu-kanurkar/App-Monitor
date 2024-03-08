package com.redwater.appmonitor.viewmodel

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redwater.appmonitor.data.model.MonthlyStats
import com.redwater.appmonitor.data.model.TimeModel
import com.redwater.appmonitor.data.model.toAppModel
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.ui.AnalyticsState
import com.redwater.appmonitor.ui.DataLoadingState
import com.redwater.appmonitor.ui.Error
import com.redwater.appmonitor.ui.PermissionPopUpState
import com.redwater.appmonitor.ui.PermissionState
import com.redwater.appmonitor.ui.PermissionType
import com.redwater.appmonitor.utils.TimeFormatUtility
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val repository: AppUsageStatsRepository): ViewModel() {
    private val TAG = "AnalyticsViewModel"
    private val permissionManager = PermissionManager()
    var permissionStateMap = mutableStateMapOf<Int, PermissionState>()
        private set
    var analyticsState: MutableState<AnalyticsState> = mutableStateOf(AnalyticsState())
        private set
    var monthlyStats = mutableStateListOf<MonthlyStats>()
        private set

    fun getMonthlyStats(packageName: String, context: Context){
        Logger.d(TAG, "getting monthly app usage")
        if (permissionManager.hasUsagePermission(context).not()){
            analyticsState.value =
                analyticsState.value.copy(showError = Error(show = true, errorMsg = "Permissions denied"), permissionPopUpState = PermissionPopUpState(show = true, permissionType = PermissionType.overlayPermission))
            return
        }else{
            analyticsState.value =
                analyticsState.value.copy(showError = Error(show = false), permissionPopUpState = PermissionPopUpState(show = false, permissionType = null))
        }
        viewModelScope.launch {
            Logger.d(TAG, "changing loader state to true")
            analyticsState.value = analyticsState.value.copy(dataLoadingState = DataLoadingState(show = true, message = "Analysing data"))
            Logger.d(TAG, "changed loader state true")
            val stats = repository.getMonthlyAppUsage(packageName = packageName, context = context.applicationContext)
            Logger.d(TAG, "monthly for $packageName : $stats")
            monthlyStats.addAll(stats)
        }
    }
    fun getPackageInfo(packageName: String,context: Context){

        Logger.d(TAG, "getting app usage")
        if (permissionManager.hasUsagePermission(context).not()){
            analyticsState.value =
                analyticsState.value.copy(showError = Error(show = true, errorMsg = "Permissions denied"), permissionPopUpState = PermissionPopUpState(show = true, permissionType = PermissionType.overlayPermission))
            return
        }else{
            analyticsState.value =
                analyticsState.value.copy(showError = Error(show = false), permissionPopUpState = PermissionPopUpState(show = false, permissionType = null))
        }
        viewModelScope.launch {
            Logger.d(TAG, "changing loader state to true")
            analyticsState.value = analyticsState.value.copy(dataLoadingState = DataLoadingState(show = true, message = "Analysing data"))
            Logger.d(TAG, "changed loader state true")
            var appUsage = repository.getAppModelData(packageName = listOf(packageName) , context = context.applicationContext)[packageName]
            Logger.d(TAG, "app model for $packageName : $appUsage")
            repository.getSavedPrefsFor(packageName = packageName).collectLatest {
                Logger.d(TAG, "collected app data: $it")
                val savedPrefs = it?.toAppModel()
                appUsage = appUsage?.copy(isSelected = savedPrefs?.isSelected?: false,
                    thresholdTime = savedPrefs?.thresholdTime,
                    delay = savedPrefs?.delay?:0,
                    dndStartTime = savedPrefs?.dndStartTime,
                    dndEndTime = savedPrefs?.dndEndTime)
                Logger.d(TAG, "changing loader state to true")
                analyticsState.value = analyticsState.value.copy(appModel = appUsage, dataLoadingState = DataLoadingState(show = false, message = null))
                Logger.d(TAG, "changed loader state false")

            }
            //The code after this collect statement is not reachable
        }
    }
    fun getPermissionState(context: Context){
        permissionStateMap.putAll(permissionManager.getPermissionState(context = context))
    }

    fun onPopUpClick(type: Int?, isPositive: Boolean, context: Context){
        if (isPositive && type != null){
            analyticsState.value = analyticsState.value.copy(permissionPopUpState = PermissionPopUpState(show = false, permissionType = null))
            permissionManager.requestPermissionSystemUI(type = type, isPositive = isPositive, context = context)
        }else{
            Logger.d(TAG, "Permission denied")
            analyticsState.value = analyticsState.value.copy(permissionPopUpState = PermissionPopUpState(show = false, permissionType = null))
        }
    }

    fun onAppPrefsClickEvent(packageName: String, isSelected: Boolean){
        Logger.d(TAG, "isSelected: $isSelected")
        if (permissionStateMap.get(PermissionType.overlayPermission)?.hasPermission == false){
            analyticsState.value = analyticsState.value.copy(permissionPopUpState = PermissionPopUpState(show = true, permissionType = PermissionType.overlayPermission))
            return
        }
        if (permissionStateMap.get(PermissionType.notificationPermission)?.hasPermission == false){
            analyticsState.value = analyticsState.value.copy(permissionPopUpState = PermissionPopUpState(show = true, permissionType = PermissionType.notificationPermission))
            return
        }
        if (isSelected){
            analyticsState.value = analyticsState.value.copy(showTimePopUp = true)
        }else{
            viewModelScope.launch {
                repository.unselectPrefsFor(packageName = packageName)
            }
        }

    }

    fun onTimeSelection(isDismiss: Boolean = false, packageName: String? = null, thresholdTimeInString: String? = null, context: Context? = null){
        if (isDismiss){
            analyticsState.value = analyticsState.value.copy(showTimePopUp = false)
        }else{
            if (packageName != null && thresholdTimeInString != null && context != null){
                analyticsState.value = analyticsState.value.copy(showTimePopUp = false)
                changeAppPrefs(thresholdTimeInString = thresholdTimeInString)
            }
        }
    }

    fun onTimeSelection(isDismiss: Boolean = false, packageName: String? = null, durationModel: TimeModel?, context: Context? = null){
        if (isDismiss){
            analyticsState.value = analyticsState.value.copy(showTimePopUp = false)
        }else{
            if (packageName != null && durationModel != null && context != null){
                analyticsState.value = analyticsState.value.copy(showTimePopUp = false)
                changeAppPrefs(durationModel = durationModel)
            }
        }
    }

    private fun changeAppPrefs(thresholdTimeInString: String){
        viewModelScope.launch {
            val thresholdTimeInMin: Short = TimeFormatUtility().getTimeInMin(thresholdTimeInString = thresholdTimeInString)
            val appInfo =  analyticsState.value.appModel
            if (appInfo != null){
                repository.insertPrefsFor(
                    appInfo.copy(isSelected = true, thresholdTime = thresholdTimeInMin)
                )
            }
        }
    }

    private fun changeAppPrefs(durationModel: TimeModel){
        viewModelScope.launch {
            val thresholdTimeInMin: Short = TimeFormatUtility().getTimeInMin(timeModel = durationModel)
            val appInfo =  analyticsState.value.appModel
            if (appInfo != null){
                repository.insertPrefsFor(
                    appInfo.copy(isSelected = true, thresholdTime = thresholdTimeInMin)
                )
            }
        }
    }
}
