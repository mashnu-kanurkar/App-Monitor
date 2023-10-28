package com.redwater.appmonitor.ui

import android.content.Context
import com.redwater.appmonitor.R


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