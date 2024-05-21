package com.redwater.appmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.model.DNDTimeType
import com.redwater.appmonitor.data.model.getDNDTimeFrom
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.ui.components.AppListContainer
import com.redwater.appmonitor.ui.components.DNDTimerDisplay
import com.redwater.appmonitor.ui.components.DragTargetInfo
import com.redwater.appmonitor.ui.components.LongPressDraggable
import com.redwater.appmonitor.ui.components.TimeSelectionDialog
import com.redwater.appmonitor.viewmodel.DNDViewModel

@Composable
fun DNDScreen(dndViewModel: DNDViewModel,
              lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
    val TAG ="DNDScreen"

    val state = remember { DragTargetInfo() }
    LongPressDraggable(
        modifier = Modifier.fillMaxSize()
    ){
        Box(modifier = Modifier.fillMaxSize()) {
            var showTimePopUp by remember {
                mutableStateOf(false)
            }
            var dndTimerKeyClicked by remember {
                mutableStateOf(Pair<String, DNDTimeType>("", DNDTimeType.START))
            }

            val unSelectedApps = remember {
                dndViewModel.unSelectedAppsList
            }

            val dndMap = remember {
                dndViewModel.dndMap
            }
            val context = LocalContext.current

            DisposableEffect(key1 = LocalLifecycleOwner.current,) {
                // Create an observer that triggers our remembered callbacks
                // for sending analytics events
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        Logger.d(TAG, "on create")
                        dndViewModel.getAppsList(context)
                    }
                }
                // Add the observer to the lifecycle
                lifecycleOwner.lifecycle.addObserver(observer)
                // When the effect leaves the Composition, remove the observer
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                dndMap.forEach { entry->
                    Logger.d(TAG, "dndKey at compose ${entry.key}")
                    val dndTime = getDNDTimeFrom(entry = entry)
                    dndTime?.let {
                        DNDTimerDisplay(
                            dndTime = it,
                            dndKey = entry.key,
                            onAddedApp = {
                                         dndViewModel.addDNDApp(appModel = it, dndKey = entry.key)
                            },
                            onStartTimeCardClicked = {key->
                                dndTimerKeyClicked = Pair(key, DNDTimeType.START)
                                showTimePopUp = true
                            },
                            onEndTimeCardClicked = {key->
                            dndTimerKeyClicked = Pair(key, DNDTimeType.END)
                            showTimePopUp = true
                        }){key, model->
                            Logger.d(TAG, "removing app from DND")
                            dndViewModel.removeDNDApp(appModel = model)
                        }
                    }
                }
                Divider(modifier = Modifier
                    .padding(0.dp, 8.dp)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = MaterialTheme.colorScheme.onSurface)
                )
                AppListContainer(
                    title = stringResource(id = R.string.non_dnd_container_title),
                    instruction = stringResource(id = R.string.non_dnd_instruction),
                    height = 720.dp,
                    appModelList = unSelectedApps,
                    context = context
                )

            }
            if (state.isDragging) {
                Logger.d("is dragging", "${state.dataToDrop}")
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(modifier = Modifier
                    .graphicsLayer {
                        val offset = (state.dragPosition + state.dragOffset)
                        scaleX = 1.3f
                        scaleY = 1.3f
                        alpha = if (targetSize == IntSize.Zero) 0f else .9f
                        translationX = offset.x.minus(targetSize.width / 2)
                        translationY = offset.y.minus(targetSize.height / 2)
                    }
                    .onGloballyPositioned {
                        targetSize = it.size
                    }
                ) {
                    state.draggableComposable?.invoke()
                }
            }
            if (showTimePopUp) {
                val descriptionWithAppName = stringResource(id = R.string.dnd_select)
                TimeSelectionDialog(title = stringResource(id = R.string.dnd_screen),
                    description = descriptionWithAppName,
                    is24Hour = false,
                    onSelection = {
                        Logger.d(TAG, "Selected time $it")
                        showTimePopUp = false
                        dndViewModel.onDNDTimeChanged(
                            oldDNDKey = dndTimerKeyClicked.first,
                            updatedTimeModel = it,
                            dndTimeType = dndTimerKeyClicked.second
                        )
                    }) {
                    showTimePopUp = false
                }
            }
        }
    }
}

