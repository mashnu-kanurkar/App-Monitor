package com.redwater.appmonitor.ui.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ironsource.mediationsdk.IronSource
import com.redwater.appmonitor.R
import com.redwater.appmonitor.advertising.ADManager
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.ui.PermissionType
import com.redwater.appmonitor.ui.components.ErrorDescriptor
import com.redwater.appmonitor.ui.components.LoadingIndicator
import com.redwater.appmonitor.ui.components.PackageInfoCard
import com.redwater.appmonitor.ui.components.PermissionAlertDialog
import com.redwater.appmonitor.ui.components.TimeSelectionDialog
import com.redwater.appmonitor.ui.components.UsageIndicator
import com.redwater.appmonitor.viewmodel.MainViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier,
               lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
               mainViewModel: MainViewModel,
               adManager: ADManager?,
               context: Context = LocalContext.current,
               onNavigateNext: (packageName: String)-> Unit,
               onNavigateToPermissionScreen: ()-> Unit
               ) {
    val TAG = "HomeScreen"
    val uiStateUnselected = remember {
        mainViewModel.uiStateUnselected
    }
    val uiStateSelected = remember {
        mainViewModel.uiStateSelected
    }
    val isLoadingData by mainViewModel.isLoadingData
    val permissionState = mainViewModel.permissionStateMap

    var showPermissionPopUp by remember {
        mutableStateOf(false)
    }
    var permissionPopUpType by remember {
        mutableStateOf<Int>(-1)
    }
    var showTimePopUpForApp by remember {
        mutableStateOf<Int?>(null)
    }
    val isServiceRunning by mainViewModel.isServiceRunning
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        mainViewModel.onPopUpClick(type = PermissionType.notificationPermission, isPositive = isGranted, context = context)
    }

    DisposableEffect(key1 = LocalLifecycleOwner.current,){
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                Logger.d(TAG, "on start")
                mainViewModel.getPermissionState(context)
                if (uiStateUnselected.isEmpty() && permissionState.get(PermissionType.usagePermission)?.hasPermission == true){
                    mainViewModel.getAppUsageTime(context)
                }else{
                    if (permissionState.get(PermissionType.usagePermission)?.hasPermission == false){
                        permissionPopUpType = PermissionType.usagePermission
                        showPermissionPopUp = true
                    }
                }
            }
        }
        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)
        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
        Box(modifier = modifier){
            Column() {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    //.padding(0.dp, 8.dp)
                    .background(
                        color = if (isServiceRunning) Color.Green else Color.Red
                    ),
                ){

                    Text(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp),
                        text = if (isServiceRunning) stringResource(id = R.string.running_service) else stringResource(id = R.string.stopped_service),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                permissionState.keys.forEach {permissionType->
                    if (permissionState[permissionType]?.hasPermission == false){
                        permissionState[permissionType]?.errorDescription?.let {
                            ErrorDescriptor(error = it){
                                permissionPopUpType = permissionType
                                showPermissionPopUp = true
                            }
                        }
                    }
                }
                Text(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                    text = stringResource(id = R.string.info))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ){
                    itemsIndexed( items = uiStateSelected){index, appInfo ->
                        val usageTime = (appInfo.usageTimeInMillis/(1000*60))
                        PackageInfoCard(
                            modifier = Modifier.padding(4.dp, 8.dp),
                            icon = appInfo.icon?:context.applicationContext.packageManager.getApplicationIcon(context.applicationContext.packageName).toBitmap(48, 48).asImageBitmap(),
                            name = appInfo.name,
                            packageName = appInfo.packageName,
                            isSelected = appInfo.isSelected,
                            index = index,
                            usageTimeInMin = usageTime.toShort(),
                            usageIndicator = {
                                UsageIndicator(usageTimeInMin = usageTime.toFloat(), thresholdTimeInMin = appInfo.thresholdTime?.toFloat())
                            },
                            onClick = {packageName ->
                                Logger.d(TAG, "Navigating with $packageName" )
                                onNavigateNext.invoke(packageName)
                            }
                        ) { cardIndex ->
                            mainViewModel.onAppUnSelected(index = cardIndex)
                            adManager?.showInterstitialAd(context as Activity)
                        }
                    }
                    itemsIndexed(uiStateUnselected){ index, appInfo ->
                        PackageInfoCard(
                            modifier = Modifier.padding(4.dp, 8.dp),
                            icon = appInfo.icon?:context.applicationContext.packageManager.getApplicationIcon(context.applicationContext.packageName).toBitmap(48, 48).asImageBitmap(),
                            name = appInfo.name,
                            packageName = appInfo.packageName,
                            isSelected = appInfo.isSelected,
                            index = index,
                            usageTimeInMin = (appInfo.usageTimeInMillis/(1000*60)).toShort(),
                            onClick = {packageName ->
                                Logger.d(TAG, "Navigating with $packageName" )
                                onNavigateNext.invoke(packageName)}
                        ) {cardIndex ->
                            if (permissionState.get(PermissionType.overlayPermission)?.hasPermission == false){
                                permissionPopUpType = PermissionType.overlayPermission
                                showPermissionPopUp = true
                                return@PackageInfoCard
                            }
                            if (permissionState.get(PermissionType.notificationPermission)?.hasPermission == false){
                                //permissionPopUpType = PermissionType.notificationPermission
                                //showPermissionPopUp = true
                                onNavigateToPermissionScreen.invoke()
                                return@PackageInfoCard
                            }
                            //ShowPopForTimeLimit
                            showTimePopUpForApp = cardIndex

                        }
                    }
                }
            }
            if (isLoadingData){
                LoadingIndicator()
            }
            if (showPermissionPopUp){
                PermissionAlertDialog(
                    title = stringResource(id = R.string.permission_required_title),
                    message = permissionState.get(permissionPopUpType)?.errorDescription?:"",
                    onClickNo = {
                        showPermissionPopUp = false
                    },
                    onClickYes = {
                        showPermissionPopUp = false
                        if (permissionPopUpType == PermissionType.notificationPermission){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }

                        }else {
                            mainViewModel.onPopUpClick(
                                type = permissionPopUpType,
                                isPositive = true,
                                context = context
                            )
                        }
                    }) {
                    showPermissionPopUp = false
                }
            }

            if (showTimePopUpForApp != null){
                val appName = uiStateUnselected[showTimePopUpForApp!!].name
                val descriptionWithAppName = stringResource(id = R.string.threshold_time_description).replace(oldValue = "##app_name##", newValue = appName)
                TimeSelectionDialog(title = stringResource(id = R.string.threshold_time_title),
                    description = descriptionWithAppName,
                    //timeList = timeList,
                    onSelection = {
                        mainViewModel.onAppSelected(
                            index = showTimePopUpForApp!!,
                            durationModel = it,)
                        showTimePopUpForApp = null
                        adManager?.showInterstitialAd(context as Activity)
                    }) {
                    showTimePopUpForApp = null
                }
            }

        }
}



