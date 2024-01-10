package com.redwater.appmonitor.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redwater.appmonitor.data.OnBoardingPreferences
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.permissions.PermissionManager
import com.redwater.appmonitor.ui.PermissionState
import com.redwater.appmonitor.ui.PermissionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PermissionViewModel: ViewModel() {

    private val TAG = this::class.simpleName
    private val _permissionStateFlow = MutableSharedFlow<Map<Int, PermissionState>>()
    val permissionStateFlow = _permissionStateFlow.asSharedFlow()
    var permissionStateMap = mutableStateMapOf<Int, PermissionState>()
        private set
    private val permissionManager = PermissionManager()
    private var flowRegistered = false
    fun onNotificationPermissionChanged(isGranted: Boolean, context: Context){
        Logger.d(TAG, "permission changed: $isGranted")
        if (flowRegistered.not()){
            //registerStateFlow(context = context)
        }
    }
    fun onPermissionClick(type: Int, isPositive: Boolean, context: Context){
        if (flowRegistered.not()){
            //registerStateFlow(context = context)
        }
        permissionManager.requestPermissionSystemUI(type = type, isPositive = isPositive, context = context)
    }

    fun updateOnboardingPreference(context: Context){
        viewModelScope.launch {
            OnBoardingPreferences(context = context).updateOnBoardingPreference(isCompleted = 1)
        }
    }

    suspend fun registerStateFlow(context: Context): Flow<Map<Int, PermissionState>> {
        return permissionManager.registerLiveUpdate(context)
    }

}