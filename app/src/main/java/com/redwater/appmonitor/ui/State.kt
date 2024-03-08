package com.redwater.appmonitor.ui

import android.content.Context
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.Blog


object PermissionType{
    const val loading = 0
    const val usagePermission = 1
    const val overlayPermission = 2
    const val notificationPermission = 3

}

abstract class PermissionState{
    abstract val type: Int
    abstract var hasPermission: Boolean
    abstract val errorDescription: String

    override fun toString(): String {
        return "{type=$type, hasPermission=$hasPermission, errorDescription=$errorDescription}"
    }
}

class NotificationPermission(val context: Context, override var hasPermission: Boolean = false) : PermissionState() {
    override val type: Int = PermissionType.notificationPermission
    override val errorDescription: String = context.resources.getString(R.string.notification_permission_description)
}

class UsagePermission(val context: Context, override var hasPermission: Boolean = false): PermissionState(){
    override val type: Int = PermissionType.usagePermission
    override val errorDescription: String = context.resources.getString(R.string.usage_data_permission_details)
}

class OverlayPermission(val context: Context, override var hasPermission: Boolean = false): PermissionState(){
    override val type: Int = PermissionType.overlayPermission
    override val errorDescription: String = context.resources.getString(R.string.overlay_permission_details)
}
data class AnalyticsState(
    val appModel: AppModel? = null,
    val longestSessionInMin: Short = 0,
    val showTimePopUp: Boolean = false,
    val dataLoadingState: DataLoadingState = DataLoadingState(show = true),
    val showError: Error = Error(show = false),
    val permissionPopUpState: PermissionPopUpState = PermissionPopUpState(show = false)
)
data class Error(val show: Boolean, val errorMsg: String? = null)

data class PermissionPopUpState(val show: Boolean, val permissionType: Int? = null)

data class DataLoadingState(val show: Boolean, val message: String? = null)

data class DashboardState(
    val quote: String = Constants.defQuote,
    val blog: Blog? = null,

)