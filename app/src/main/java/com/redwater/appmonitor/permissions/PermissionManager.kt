package com.redwater.appmonitor.permissions

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.patrykandpatrick.vico.compose.state.mutableSharedStateOf
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.ui.NotificationPermission
import com.redwater.appmonitor.ui.OverlayPermission
import com.redwater.appmonitor.ui.PermissionState
import com.redwater.appmonitor.ui.PermissionType
import com.redwater.appmonitor.ui.UsagePermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PermissionManager {
    private val TAG = this::class.simpleName

    // 0 = MODE_ALLOWED
    fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            0 == appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }else{
            0 == appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
    }

    fun hasOverlayPermission(context: Context):Boolean{
        return Settings.canDrawOverlays(context)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Logger.d(TAG, "hasNotificationPermission: true")
                return true
            }
            Logger.d(TAG, "hasNotificationPermission: false")
            return false
        }
        Logger.d(TAG, "hasNotificationPermission: true")
        return true
    }

    fun requestPermission(context: ComponentActivity, onPermissionGranted: (Boolean)->Unit ): ActivityResultLauncher<String> {
        return context.registerForActivityResult(ActivityResultContracts.RequestPermission()){
            onPermissionGranted.invoke(it)
        }
    }

    fun requestPermissionSystemUI(type: Int, isPositive: Boolean, context: Context){
        when(type){
            PermissionType.usagePermission ->{
                if (isPositive && this.hasUsagePermission(context).not())
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            PermissionType.overlayPermission ->{
                if (isPositive && this.hasOverlayPermission(context).not())
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
            }
            PermissionType.notificationPermission ->{
                Logger.d(TAG, "type: $type isPositive: $isPositive ")
                requestPermission(context = (context) as ComponentActivity){
                    if (it) Logger.d(TAG, "Notification permission granted")
                }
            }
        }
    }

    fun getPermissionState(context: Context): Map<Int, PermissionState>{
        Logger.d(TAG, "getting permission")
        val usagePermission = this.hasUsagePermission(context)
        val overlayPermission = this.hasOverlayPermission(context)
        var notificationPermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            notificationPermission = this.hasNotificationPermission(context)
        }
        return mapOf(PermissionType.usagePermission to UsagePermission(context, usagePermission), PermissionType.overlayPermission to OverlayPermission(context, overlayPermission), PermissionType.notificationPermission to NotificationPermission(context, notificationPermission))
    }

    suspend fun registerLiveUpdate(context: Context): Flow<Map<Int, PermissionState>> = flow {
        while (true){
            emit(getPermissionState(context = context))
            delay(2000)
        }
    }

}