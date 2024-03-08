package com.redwater.appmonitor.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.DNDTime
import com.redwater.appmonitor.data.model.TimeModel
import com.redwater.appmonitor.logger.Logger
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp


@Composable
fun DNDCard(modifier: Modifier = Modifier,
                    title: String,
                    instruction: String,
                    dndTime: DNDTime,
                    dndKey: String,
                    onAddedApp: (AppModel, String) -> Unit,
                    onStartTimeCardClicked: (String)->Unit,
                    onEndTimeCardClicked: (String)->Unit,
                    onDNDAppClick: (String, AppModel)->Unit
) {
    val endError = dndTime.startTime == dndTime.endTime
    val context = LocalContext.current
    Card(modifier = modifier
        .fillMaxWidth()
        .padding(4.dp),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(IntrinsicSize.Min)) {
            DNDTimeCard(
                dndKey = dndKey,
                text = "From",
                timeModel = dndTime.startTime,
                onTimeCardClicked = {
                    onStartTimeCardClicked.invoke(it)
                }
            )
            Divider(modifier = Modifier
                .padding(8.dp, 0.dp)
                .fillMaxHeight()
                .width(1.dp)
                .background(color = MaterialTheme.colorScheme.onPrimaryContainer)
            )
            DNDTimeCard(
                dndKey = dndKey,
                text = "To",
                timeModel = dndTime.endTime,
                onTimeCardClicked = {
                    onEndTimeCardClicked.invoke(it)
                }
            )
        }
        if (endError){
            RWBodyText(text = stringResource(id = R.string.dnd_error), isError = true)
        }
        Divider(modifier = Modifier
            .padding(0.dp, 8.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(color = MaterialTheme.colorScheme.onSecondaryContainer)
        )
        DNDAppDropContainer(title = title,
            instruction = instruction,
            height = 144.dp,
            appModelList = dndTime.appModelMap.values.toList(),
            context = context,
            onAddedApp = {
                onAddedApp.invoke(it, dndKey)
            }){
            onDNDAppClick.invoke(dndKey, it)
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.DNDTimeCard(dndKey: String,
                          text: String,
                          timeModel: TimeModel,
                          isError: Boolean = false,
                          onTimeCardClicked: (String)->Unit, ) {

    Card(modifier = Modifier
        .padding(4.dp)
        .weight(1f),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = if (isError) BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.error) else null,
        shape = RoundedCornerShape(8.dp),
        onClick = { onTimeCardClicked.invoke(dndKey) }
    ){
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween) {
            RWBodyText(modifier = Modifier.padding(4.dp), text = text )
            RWBigDisplayText(modifier = Modifier.padding(4.dp),
                text = "${timeModel.hour} : ${timeModel.minute}")
            RWItalicText(modifier = Modifier.padding(4.dp), text = "${timeModel.period}")
        }
    }
}
@Composable
fun ColumnScope.DNDAppDropContainer(title: String,
                                     instruction: String,
                                     height: Dp = 216.dp,
                                     appModelList: List<AppModel>,
                                     context: Context,
                                     onAddedApp: (AppModel) -> Unit,
                                     onClick: (AppModel) -> Unit) {
    Logger.d("drag container", "app list ${appModelList.toList()}")
    RWHeaderText(text = title)
    RWItalicText(text = instruction)
    Divider(modifier = Modifier
        .padding(0.dp, 8.dp)
        .fillMaxWidth()
        .height(1.dp)
        .background(color = MaterialTheme.colorScheme.onPrimaryContainer)
    )
    DropContainer(
        modifier = Modifier,
        appModelList = appModelList,
        height = height,
        context = context,
        onAddedApp = onAddedApp,
        onClick = onClick)
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun DropContainer(modifier : Modifier,
                  appModelList: List<AppModel>,
                  height: Dp = 216.dp,
                  context: Context,
                  onAddedApp: (AppModel)->Unit,
                  onClick: (AppModel)-> Unit
) {
    val dragInfo = LocalDragTargetInfo.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    var isCurrentDropTarget by remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            Logger.d("DropContainer", "rect top left: ${rect.topLeft}, bottom right: ${rect.bottomRight}")
            Logger.d("DropContainer", "position ${dragPosition + dragOffset}")
            isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
        }
    }) {
        Logger.d("DropContainer", "isDrop target: $isCurrentDropTarget, ")
        val appModel =
            if (isCurrentDropTarget && !dragInfo.isDragging) dragInfo.dataToDrop as AppModel? else null
        val bgColor = if (isCurrentDropTarget) Color.Red else Color.Black
        appModel?.let {
            if (isCurrentDropTarget){
                onAddedApp.invoke(it)
            }
        }
        Surface(modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = height + 20.dp)
            .background(color = bgColor,),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, color = bgColor)) {

            LazyVerticalGrid(
                modifier = Modifier.height(height),
                columns = GridCells.Adaptive(72.dp),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Top
            ){
                items(appModelList){
                    Column {
                        val textMeasurer = rememberTextMeasurer()
                        Image(modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                            .drawWithCache {
                                val path = Path()
                                path.addOval(
                                    Rect(
                                        topLeft = Offset.Zero,
                                        bottomRight = Offset(size.width, size.height)
                                    )
                                )
                                onDrawWithContent {
                                    clipPath(path) {
                                        // this draws the actual image - if you don't call drawContent, it wont
                                        // render anything
                                        this@onDrawWithContent.drawContent()
                                    }
                                    val dotSize = size.width / 4f
                                    // Clip a white border for the content
                                    drawCircle(
                                        Color.Black,
                                        radius = dotSize,
                                        center = Offset(
                                            x = size.width - dotSize,
                                            y = dotSize
                                        ),
                                        blendMode = BlendMode.Clear
                                    )
                                    // draw the red circle indication
                                    drawCircle(
                                        Color(0xFF000000),
                                        radius = dotSize * 0.8f,
                                        center = Offset(
                                            x = size.width - dotSize,
                                            y = dotSize
                                        )
                                    )
                                    val measuredText =
                                        textMeasurer.measure(
                                            text = "X",
                                            style = TextStyle(fontSize = 16.sp)
                                        )
                                    drawText(
                                        textLayoutResult = measuredText,
                                        color = Color.White,
                                        topLeft = Offset(
                                            x = (size.width - dotSize - (measuredText.size.width / 2)),
                                            y = (dotSize - (measuredText.size.height / 2))
                                        )
                                    )
                                }
                            }
                            .clickable {
                                Logger.d("DNDAppContainer", "app icon clicked")
                                onClick.invoke(it)
                            },
                            bitmap = it.icon?: context.applicationContext.packageManager.getApplicationIcon(context.applicationContext.packageName).toBitmap(48, 48).asImageBitmap(),
                            contentDescription = "app icon")
                        RWBodyText(text = it.name)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

}

@Composable
fun AllAppContainer(title: String,
                     instruction: String,
                     height: Dp = 216.dp,
                     appModelList: List<AppModel>,
                     context: Context,) {
    Column(modifier = Modifier.fillMaxWidth()) {
        RWHeaderText(text = title)
        RWItalicText(text = instruction)
        Divider(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp))
        Surface(modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = height + 20.dp)
            .background(color = MaterialTheme.colorScheme.onSecondaryContainer),
            shape = RoundedCornerShape(8.dp)) {
            LazyVerticalGrid(
                modifier = Modifier.height(height),
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Top
            ){
                items(appModelList){
                    Logger.d("App container", "$it")
                    AppDataHolder(appModel = it, context = context)
                }
            }
        }

    }

}

@Composable
fun AppDataHolder(appModel: AppModel, context: Context) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    val currentState = LocalDragTargetInfo.current
    Card(elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxHeight()
            .onGloballyPositioned {
                currentPosition = it.localToWindow(Offset.Zero)
            }
            .pointerInput(appModel) {
                detectDragGesturesAfterLongPress(onDragStart = {
                    Logger.d("AppDataHolder", "dataToDrop 0 : $appModel")
                    currentState.dataToDrop = appModel
                    Logger.d("AppDataHolder", "currentState 1 :${currentState.dataToDrop}")
                    currentState.isDragging = true
                    currentState.dragPosition = currentPosition + it
                    currentState.draggableComposable = {
                        DraggableApp(
                            appModel = appModel,
                            context = context
                        )
                    }
                    Logger.d("AppDataHolder", "drag position: $${currentState.dragPosition}")
                }, onDrag = { change, dragAmount ->
                    change.consume()
                    currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                    Logger.d("AppDataHolder", "offset: ${currentState.dragOffset}")
                    Logger.d("AppDataHolder", "data: ${currentState.dataToDrop}")
                }, onDragEnd = {
                    currentState.isDragging = false
                    currentState.dragOffset = Offset.Zero
                }, onDragCancel = {
                    currentState.dragOffset = Offset.Zero
                    currentState.isDragging = false
                })
            }
    ) {
        Image(modifier = Modifier
            .padding(4.dp)
            .size(52.dp)
            .clip(RoundedCornerShape(2.dp)),
            bitmap = appModel.icon?: context.applicationContext.packageManager.getApplicationIcon(context.applicationContext.packageName).toBitmap(48, 48).asImageBitmap(),
            contentDescription = "app icon")
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = appModel.name, maxLines = 1, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))

    }
}

@Composable
fun DraggableApp(appModel: AppModel, context: Context) {
    Box(modifier = Modifier){
        Image(modifier = Modifier
            .padding(4.dp)
            .size(52.dp)
            .clip(RoundedCornerShape(2.dp)),
            bitmap = appModel.icon?: context.applicationContext.packageManager.getApplicationIcon(context.applicationContext.packageName).toBitmap(48, 48).asImageBitmap(),
            contentDescription = "app icon")
    }
}