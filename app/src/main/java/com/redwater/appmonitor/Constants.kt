package com.redwater.appmonitor

object Constants {

    const val appDatabase = "app_database"
    const val appPrefsTable = "app_prefs"

    object AppPrefsColumns{
        const val name = "name"
        const val packageName = "package"
        const val isSelected = "is_selected"
        const val usageTime = "usage_time"
        const val thresholdTime = "thr_time"
        const val icon = "icon"
    }

    const val foregroundAppWorkerTag = "ForegroundAppWorker"
    const val foregroundAppWorkerPeriodInMin = 60L
}