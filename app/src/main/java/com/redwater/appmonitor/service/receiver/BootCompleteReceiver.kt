package com.redwater.appmonitor.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.redwater.appmonitor.service.ServiceManager

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            context?.let { ServiceManager.startService(context) }
        }
    }
}