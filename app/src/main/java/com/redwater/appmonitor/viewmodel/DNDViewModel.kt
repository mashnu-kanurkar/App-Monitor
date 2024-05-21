package com.redwater.appmonitor.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.DNDTimeType
import com.redwater.appmonitor.data.model.Period
import com.redwater.appmonitor.data.model.TimeModel
import com.redwater.appmonitor.data.model.createDNDKey
import com.redwater.appmonitor.data.model.toAppModel
import com.redwater.appmonitor.data.model.toDNDMap
import com.redwater.appmonitor.data.model.toFormattedString
import com.redwater.appmonitor.data.model.updateDNDKey
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DNDViewModel(private val usageStatsRepository: AppUsageStatsRepository): ViewModel() {
    private val TAG = this::class.simpleName
    var unSelectedAppsMap = mutableStateMapOf<String, AppModel>()
        private set
    var unSelectedAppsList = mutableStateListOf<AppModel>()
        private set

    var isLoadingState = mutableStateOf(false)
        private set

    var dndMap = mutableStateMapOf<String, MutableMap<String, AppModel>>()
        private set

    private var isTimeChanged = false

    fun getAppsList(context: Context){
        Logger.d(TAG, "fetching DND records")
        isLoadingState.value = true
        viewModelScope.launch {
            val allApps = usageStatsRepository.getAppModelData(context = context)
            usageStatsRepository.getAllRecordsFlow().collectLatest {savedApps->
                val dndEnabledApps = mutableListOf<AppModel>()
                unSelectedAppsMap.putAll(allApps)
                savedApps.forEach {
                    if (it.dndStartTime != null) {
                        dndEnabledApps.add(it.toAppModel().copy(icon = allApps.get(it.packageName)?.icon))
                        unSelectedAppsMap.remove(it.name)
                    }else{
                        unSelectedAppsMap.put(it.packageName, it.toAppModel().copy(icon = allApps.get(it.packageName)?.icon))
                    }
                }
                if (unSelectedAppsList.isEmpty().not()){
                    unSelectedAppsList.clear()
                }
                unSelectedAppsList.addAll(unSelectedAppsMap.values)
                unSelectedAppsList.sortWith(compareBy { it.packageName })
                if (dndEnabledApps.isEmpty()){
                    dndMap.clear()
                    val temporaryKey = createDNDKey(startTime = TimeModel(hour = 10, minute = 30, period = Period.PM),
                        endTime = TimeModel(hour = 6, minute = 30, period = Period.AM)
                    )
                    dndMap.put(temporaryKey, mutableMapOf())
                }else{
                    dndMap.putAll(dndEnabledApps.toDNDMap())
                }
            }

        }
    }
    fun addDNDApp(appModel: AppModel, dndKey: String){
        Logger.d(TAG, "addDND appModel: $appModel")
        val (start, end) = dndKey.split("_")
        viewModelScope.launch {
            usageStatsRepository.insertPrefsFor(appModel.copy(dndStartTime = start, dndEndTime = end))
        }
    }

    fun removeDNDApp(appModel: AppModel){
        Logger.d(TAG, "removing app from DND $appModel")
        viewModelScope.launch {
            usageStatsRepository.disableDNDFor(appModel.packageName)
        }
    }

    fun onDNDTimeChanged(oldDNDKey: String, updatedTimeModel: TimeModel, dndTimeType: DNDTimeType){
        isTimeChanged = true
        val updatedList = mutableListOf<AppModel>()
        Logger.d(TAG, "updated time model $updatedTimeModel")
        when(dndTimeType){
            DNDTimeType.START ->{
                val oldList = dndMap.get(oldDNDKey)?.values
                oldList?.forEach {
                    updatedList.add(it.copy(dndStartTime = updatedTimeModel.toFormattedString()))
                }
            }
            DNDTimeType.END ->{
                val oldList = dndMap.get(oldDNDKey)?.values
                oldList?.forEach {
                    updatedList.add(it.copy(dndEndTime = updatedTimeModel.toFormattedString()))
                }
            }
        }
        viewModelScope.launch {
            dndMap.remove(oldDNDKey)
            if (updatedList.isEmpty()){
                dndMap.put(updateDNDKey(oldKey = oldDNDKey, timeModel = updatedTimeModel, dndTimeType = dndTimeType), mutableMapOf())
            }else{
                usageStatsRepository.insertPrefsFor(updatedList)
            }

        }
    }

}