package com.redwater.appmonitor.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager

class NetworkManager {
    fun isNetworkOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                ?: // lets be optimistic, if we are truly offline we handle the exception
                return true
            @SuppressLint("MissingPermission") val netInfo = cm.activeNetworkInfo
            netInfo != null && netInfo.isConnected
        } catch (ignore: Throwable) {
            // lets be optimistic, if we are truly offline we handle the exception
            true
        }
    }
}