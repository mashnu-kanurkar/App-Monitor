package com.redwater.appmonitor.logger

import android.util.Log
import java.lang.Exception

object Logger {

    private var logLevel = LogLevel.DEBUG

    fun setLogLevel(level: LogLevel) {
        logLevel = level
    }

    fun v(tag: String?, message: String){
        if (logLevel <=LogLevel.VERBOSE){
            Log.v(tag, message)
        }
    }

    fun d(tag: String?, message: String) {
        if (logLevel <= LogLevel.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String?, message: String) {
        if (logLevel <= LogLevel.INFO) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String?, message: String) {
        if (logLevel <= LogLevel.WARNING) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String?, message: String, throwable: Throwable? = null) {
        if (logLevel <= LogLevel.ERROR) {
            Log.e(tag, message, throwable)
        }
    }
}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    OFF
}