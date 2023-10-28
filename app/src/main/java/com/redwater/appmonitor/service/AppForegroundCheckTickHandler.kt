package com.redwater.appmonitor.service

import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppForegroundCheckTickHandler(private val serviceScope: CoroutineScope,
                                    private val tickIntervalMs: Long = 5000) {
    // Backing property to avoid flow emissions from other classes
    private val _tickFlow = MutableSharedFlow<Unit>(replay = 0)
    val tickFlow = _tickFlow.asSharedFlow()
    private val TAG = "AppForegroundCheckTickHandler"

    init {
        serviceScope.launch {
            while (true){
                ensureActive()
                _tickFlow.emit(Unit)
                Logger.d("$TAG => emitted tick")
                delay(tickIntervalMs)
            }
        }
    }

}