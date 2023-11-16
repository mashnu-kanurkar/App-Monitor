package com.redwater.appmonitor.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.redwater.appmonitor.logger.Logger

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
                Logger.d(TAG, "hasPermission: true")
                return true
            }
            Logger.d(TAG, "hasPermission1: false")
            return false
        }
        Logger.d(TAG, "hasPermission2: true")
        return true
    }

    fun requestPermission(context: ComponentActivity, onPermissionGranted: (Boolean)->Unit ): ActivityResultLauncher<String> {
        return context.registerForActivityResult(ActivityResultContracts.RequestPermission()){
            onPermissionGranted.invoke(it)
        }
    }

}