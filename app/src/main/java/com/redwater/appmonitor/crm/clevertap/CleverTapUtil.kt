package com.redwater.appmonitor.crm.clevertap

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI

object CleverTapUtil {

    @SuppressLint("StaticFieldLeak")
    private var cleverTapInstance: CleverTapAPI? = null

    fun init(context: Context) {
        cleverTapInstance = CleverTapAPI.getDefaultInstance(context)
    }

    // Call this after login to set user profile
    fun setUserProfile(
        identity: String,
    ) {
        val profileUpdate = HashMap<String, Any>()
        profileUpdate["Identity"] = identity
        cleverTapInstance?.onUserLogin(profileUpdate)
        Log.d("CleverTapUtil", "User profile updated: $profileUpdate")
    }

    // Track a generic event with optional properties
    fun pushEvent(eventName: String, properties: Map<String, Any>? = null) {
        if (properties != null) {
            cleverTapInstance?.pushEvent(eventName, properties)
        } else {
            cleverTapInstance?.pushEvent(eventName)
        }
        Log.d("CleverTapUtil", "Event pushed: $eventName with $properties")
    }

    fun pushNotificationClickedEvent(extras: Bundle?) {
        cleverTapInstance?.pushNotificationClickedEvent(extras)
    }

    fun createNotificationChannel(context: Context){
        CleverTapAPI.createNotificationChannel(context.applicationContext,
            "promotional",
            "Promotional",
            "Promotional",
            NotificationManager.IMPORTANCE_MAX,true)
    }
}

