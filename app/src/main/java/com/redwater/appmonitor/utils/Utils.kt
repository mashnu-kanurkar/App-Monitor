package com.redwater.appmonitor.utils

import android.content.Context
import android.content.Intent
import com.redwater.appmonitor.data.model.AppModel


fun List<AppModel>.toAppTimeMap(): Map<String, AppModel>{
    val map = hashMapOf<String, AppModel>()
    forEach {
        map[it.packageName] = it
    }
    return map
}