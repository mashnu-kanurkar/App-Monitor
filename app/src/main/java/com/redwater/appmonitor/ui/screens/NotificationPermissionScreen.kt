package com.redwater.appmonitor.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.viewmodel.PermissionViewModel
import com.redwater.appmonitor.R
import com.redwater.appmonitor.ui.PermissionType
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PermissionScreen(permissionViewModel: PermissionViewModel,
                     onNavigateNext: ()->Unit) {

    val TAG = "PermissionScreen"
    val context = LocalContext.current

    var permissionStep by remember {
        mutableStateOf(PermissionType.usagePermission)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.RequestPermission() ){ isGranted: Boolean ->
        // Handle permission result here
        if (isGranted) {
            permissionViewModel.onNotificationPermissionChanged(isGranted = true, context = context)
        } else {
            // Permission denied, handle accordingly
            permissionViewModel.onNotificationPermissionChanged(isGranted = true, context = context)
        }
    }

    LaunchedEffect(key1 = Unit){
        Logger.d(TAG, "Checking current step")
        //permissionViewModel.getPermissionState(context = context)
        permissionViewModel.registerStateFlow(context = context).collectLatest {permissionState->
            Logger.d(TAG, "0: ${permissionState.get(PermissionType.usagePermission)?.hasPermission == false}")
            permissionStep = if (permissionState.get(PermissionType.usagePermission)?.hasPermission == false){
                PermissionType.usagePermission
            }else if (permissionState.get(PermissionType.overlayPermission)?.hasPermission == false){
                PermissionType.overlayPermission
            }else if (permissionState.get(PermissionType.notificationPermission)?.hasPermission == false){
                PermissionType.notificationPermission
            }else{
                permissionViewModel.updateOnboardingPreference(context = context)
                -1
            }
            Logger.d(TAG, "Current step is $permissionStep")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        when(permissionStep){
            PermissionType.usagePermission ->{
                UsageMonitorPermissionContent {
                    permissionViewModel.onPermissionClick(type = PermissionType.usagePermission, isPositive = true, context = context)
                }
            }
            PermissionType.overlayPermission ->{
                OverlayPermissionContent {
                    permissionViewModel.onPermissionClick(type = PermissionType.overlayPermission, isPositive = true, context = context)
                }
            }
            PermissionType.notificationPermission ->{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    NotificationPermissionContent {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }else{
                    Logger.d("Permission", "No need")
                    onNavigateNext.invoke()
                }
            }
            -1 ->{
                Text(text = stringResource(id = R.string.all_granted))
                Button(onClick = { onNavigateNext.invoke() }) {
                    Text(text = stringResource(id = R.string.home_screen))
                }
                LaunchedEffect(key1 = Unit){
                    Logger.d("Permission", "Moving to next")
                    onNavigateNext.invoke()
                }
            }

        }

    }
}

@Composable
fun NotificationPermissionContent(onClickAllow: ()-> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(modifier = Modifier.padding(4.dp, 16.dp),painter = painterResource(id = R.drawable.icon_bell), contentDescription = "bell")
        Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.notification_permission_title), style = MaterialTheme.typography.titleLarge)
        Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.notification_permission_description))
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onClickAllow) {
            Text(modifier = Modifier.padding(16.dp, 4.dp), text = stringResource(id = R.string.button_allow))
        }
    }

}

@Composable
fun UsageMonitorPermissionContent(onClickAllow: ()-> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(modifier = Modifier.padding(4.dp, 16.dp),painter = painterResource(id = R.drawable.monitoring), contentDescription = "bell")
        Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.permission_required_title), style = MaterialTheme.typography.titleLarge)
        Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.usage_data_permission_details))
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onClickAllow) {
            Text(modifier = Modifier.padding(16.dp, 4.dp), text = stringResource(id = R.string.button_allow))
        }
    }
}

@Composable
fun OverlayPermissionContent(onClickAllow: ()-> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(modifier = Modifier.padding(4.dp, 16.dp),painter = painterResource(id = R.drawable.layers), contentDescription = "bell")
        Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.permission_required_title), style = MaterialTheme.typography.titleLarge)
        Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.usage_data_permission_details))
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onClickAllow) {
            Text(modifier = Modifier.padding(16.dp, 4.dp), text = stringResource(id = R.string.button_allow))
        }
    }
}

@Preview
@Composable
fun NotificationPermissionContentPreview() {
    UsageMonitorPermissionContent({})
}
