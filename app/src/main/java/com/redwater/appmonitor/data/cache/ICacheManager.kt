package com.redwater.appmonitor.data.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class ICacheManager<T> {

    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    abstract var lastSavedTs: Long
    abstract var thresholdTime: Long
    abstract fun store(data: T)

    abstract fun getCacheData(): T?

    abstract fun flush()
}

