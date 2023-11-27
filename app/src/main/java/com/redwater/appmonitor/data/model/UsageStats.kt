package com.redwater.appmonitor.data.model

data class AppDataFromSystem(
    val packageName: String = "",
    val usageTime: Short = 0,
    val usageDist: MutableMap<Short, Long> = mutableMapOf(),
)