package com.redwater.appmonitor.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger


internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

@Composable
fun LongPressDraggable(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(
        LocalDragTargetInfo provides state
    ) {
        Box(modifier = modifier.fillMaxSize())
        {
            content()
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
        }
    }
}

@Composable
fun <T> DragTarget(
    modifier: Modifier,
    dataToDrop: T,
    content: @Composable (() -> Unit)
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    val currentState = LocalDragTargetInfo.current
    Logger.d("dragTarget", "currentState 0 : ${currentState.dataToDrop}")
    Box(modifier = modifier
        .onGloballyPositioned {
            currentPosition = it.localToWindow(Offset.Zero)
        }
        .pointerInput(dataToDrop) {
            detectDragGesturesAfterLongPress(onDragStart = {
                Logger.d("dragTarget", "dataToDrop 0 : $dataToDrop")
                currentState.dataToDrop = dataToDrop
                Logger.d("dragTarget", "currentState 1 :${currentState.dataToDrop}")
                currentState.isDragging = true
                currentState.dragPosition = currentPosition + it
                currentState.draggableComposable = content
            }, onDrag = { change, dragAmount ->
                change.consume()
                currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
            }, onDragEnd = {
                currentState.isDragging = false
                currentState.dragOffset = Offset.Zero
            }, onDragCancel = {
                currentState.dragOffset = Offset.Zero
                currentState.isDragging = false
            })
        }) {
        content()
    }
}

@Composable
fun <T> DropTarget(
    modifier: Modifier,
    content: @Composable() (BoxScope.(isInBound: Boolean, data: T?) -> Unit)
) {

    val dragInfo = LocalDragTargetInfo.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    var isCurrentDropTarget by remember {
        mutableStateOf(false)
    }

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
        }
    }) {
        val data =
            if (isCurrentDropTarget && !dragInfo.isDragging) dragInfo.dataToDrop as T? else null
        content(isCurrentDropTarget, data)
    }
}

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
}

data class FoodItem(val id: Int, val name: String, val price: Double, @DrawableRes val image: Int)

val foodList = listOf(
    FoodItem(1, "Pizza", 20.0, R.drawable.food_pizza),
    FoodItem(2, "French toast", 10.05, R.drawable.food_toast),
    FoodItem(3, "Chocolate cake", 12.99, R.drawable.food_cake),
)

data class Person(val id: Int, val name: String, @DrawableRes val profile: Int)

val persons = listOf(
    Person(1, "Jhone", R.drawable.user_one),
    Person(2, "Eyle", R.drawable.user_two),
    Person(3, "Tommy", R.drawable.user_three),
)