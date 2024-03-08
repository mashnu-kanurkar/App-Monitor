package com.redwater.appmonitor.utils

import com.redwater.appmonitor.data.model.AppModel


fun List<AppModel>.toAppTimeMap(): Map<String, AppModel>{
    val map = hashMapOf<String, AppModel>()
    forEach {
        map[it.packageName] = it
    }
    return map
}