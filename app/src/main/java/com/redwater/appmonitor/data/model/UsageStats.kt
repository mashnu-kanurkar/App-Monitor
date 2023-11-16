package com.redwater.appmonitor.data.model

data class AppAndTime(
    val packageName: String = "",
    val time: Short = 0,
    val usageDist: MutableMap<Short, Long> = mutableMapOf()
)