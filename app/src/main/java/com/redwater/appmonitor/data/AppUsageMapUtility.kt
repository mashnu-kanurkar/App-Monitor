package com.redwater.appmonitor.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.redwater.appmonitor.data.model.AppModel

class AppUsageMapUtility(private var allAppUsageMap: MutableMap<String, AppModel>) {
    private var partition = allAppUsageMap.values.partition { it.isSelected }
    var selected = mutableStateListOf<AppModel>()
        private set
    var unselected = mutableStateListOf<AppModel>()
        private set

    init {

    }
    private fun updateSelectedApps(): SnapshotStateList<AppModel> {
        selected.clear()
        selected.addAll(partition.first)
        return selected
    }

    private fun updateUnSelectedApps():SnapshotStateList<AppModel>{
        unselected.clear()
        unselected.addAll(partition.second)
        unselected.sortWith(compareBy { it.usageTimeInMillis })
        unselected.sortWith(compareBy { it.name })
        return unselected
    }

    fun updateMap(map: MutableMap<String, AppModel>){
        allAppUsageMap.putAll(map)
        updateMap(allAppUsageMap)
    }

    fun updateMap(key: String, value: AppModel){
        allAppUsageMap.put(key, value)
        partition = allAppUsageMap.values.partition { it.isSelected }
        updateSelectedApps()
        updateUnSelectedApps()
    }

}