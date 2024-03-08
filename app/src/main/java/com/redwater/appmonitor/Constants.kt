package com.redwater.appmonitor

object Constants {
    const val appDatabase = "app_database"
    const val appPrefsTable = "app_prefs"
    const val overlayDataTable = "overlay_data"
    object AppPrefsColumns{
        const val name = "name"
        const val packageName = "package"
        const val isSelected = "is_selected"
        const val usageTime = "usage_time"
        const val thresholdTime = "thr_time"
        const val icon = "icon"
        const val delay = "delay"
    }
    object OverlayDataColumns{
        const val id = "id"
        const val type = "type"
        const val subType = "sub_type"
        const val data = "data"
        const val difficultyLevel = "diff_level"
        const val isUsed = "is_used"
    }
    const val foregroundAppWorkerTag = "ForegroundAppWorker"
    const val foregroundAppWorkerPeriodInMin = 60L
    const val firebaseSyncWorkerTag = "FirebaseSyncWorker"
    const val firebaseSyncWorkerPeriodInHour = 8L

    const val defPuzzleText = "{\"que\":\"10 - 86 + 25 + 80 = ?\",\"time\":\"9\",\"options\":[{\"text\":\"27.7\",\"color\":\"null\",\"is_answer\":false},{\"text\":\"8.5\",\"color\":\"null\",\"is_answer\":false},{\"text\":\"29.0\",\"color\":\"null\",\"is_answer\":true},{\"text\":\"22.9\",\"color\":\"null\",\"is_answer\":false}]}"
    const val defImagePuzzle = ""
    const val defMeme = ""
    const val defQuote = "Change might not be fast and it isn\'t always easy. But with time and effort, almost any habit can be reshaped. \$Author-Charles Duhigg"

    const val privacyPolicyURL = "https://www.freeprivacypolicy.com/live/53699ba9-d108-4b57-a71b-21961c539fe1"
}