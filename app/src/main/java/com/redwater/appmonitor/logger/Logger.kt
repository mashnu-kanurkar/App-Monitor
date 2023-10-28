package com.redwater.appmonitor.logger

import android.util.Log

object Logger {
    private const val TAG = "AppMonitor"
    private var logLevel = LogLevel.OFF

    fun setLogLevel(level: LogLevel) {
        logLevel = level
    }

    fun d(message: String) {
        if (logLevel <= LogLevel.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        if (logLevel <= LogLevel.INFO) {
            Log.i(TAG, message)
        }
    }

    fun w(message: String) {
        if (logLevel <= LogLevel.WARNING) {
            Log.w(TAG, message)
        }
    }

    fun e(message: String) {
        if (logLevel <= LogLevel.ERROR) {
            Log.e(TAG, message)
        }
    }
}

enum class LogLevel {
    INFO,
    DEBUG,
    WARNING,
    ERROR,
    OFF
}