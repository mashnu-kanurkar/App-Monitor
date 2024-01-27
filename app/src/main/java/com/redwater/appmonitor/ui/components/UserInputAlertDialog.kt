package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redwater.appmonitor.data.model.TimeModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionDialog(title: String,
                        description: String,
                        timeList: List<String>,
                        onSelection: (TimeModel)->Unit,
                        onDismiss: ()-> Unit,
) {
    val selectedIndex by remember {
        mutableStateOf(0)
    }
    val timeState = rememberTimePickerState(1, 30, true)
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(onClick = {onSelection.invoke(TimeModel(hour = timeState.hour, minute = timeState.minute))}) {
                Text(text = "Done")
            }
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold,)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), text = description, fontWeight = FontWeight.Normal,)
//                UserSelectionExposedDropdownMenuBox(modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                    itemList = timeList,
//                    selectedIndex = selectedIndex,
//                    onSelectionChanged ={index -> selectedIndex = index}
//                )
                TimePicker(state = timeState, layoutType = TimePickerLayoutType.Vertical)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectionExposedDropdownMenuBox(
    modifier: Modifier = Modifier,
    itemList: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = itemList[selectedIndex],
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                itemList.forEachIndexed{ index, item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            onSelectionChanged.invoke(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ClockSpherePreview() {
    ClockFaceTimeSelector()
}
@Composable
fun ClockFaceTimeSelector() {
    Box(modifier = Modifier){
        ClockSphere()
        ClockHourAndMinuteMarks()
    }
}

@Composable
fun ClockSphere(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val clockRadius = 0.9f * size.minDimension / 2f
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = clockRadius,
            style = Stroke(
                width = 3.dp.toPx()
            ),
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ClockHourAndMinuteMarks(
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {

        val hourMarkStyle = Fill
        val minuteMarkStyle = Stroke(width = 1.dp.toPx())
        val hourMarkRadius = 5.dp.toPx()
        val minuteMarkRadius = 2.dp.toPx()
        repeat(60) {
            val clockRadius = 0.80f * size.minDimension / 2f
            val initialDegrees = -(PI / 2f)
            val secondsToRadians = PI / 30f
            val degree = initialDegrees + it * secondsToRadians
            val x = center.x + cos(degree) * clockRadius
            val y = center.y + sin(degree) * clockRadius
            val isHourMark = it % 5 == 0
            val style = if (isHourMark) hourMarkStyle else minuteMarkStyle
            val radius = if (isHourMark) hourMarkRadius else minuteMarkRadius
            val textLayoutResult: TextLayoutResult =
                textMeasurer.measure(text = AnnotatedString(it.toString()))
            val textSize = textLayoutResult.size

            val xFactor = 1/(cos(degree)*textSize.width)
            val yFactor = 1/(sin(degree)*textSize.height)

            if (isHourMark){
                drawRect(color = Color.White,
                    topLeft = Offset((x - textSize.width/2).toFloat(), (y - textSize.height/2).toFloat()),
                    size = Size(72f, 72f),
                    style = Stroke(width = 5f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = it.toString(),
                    style = TextStyle(color = Color.White, fontSize = 24.sp),
                    topLeft = Offset((x - textSize.width/2).toFloat(), (y - textSize.height/2).toFloat()),
                    //size = textSize
                )

            }
//            drawCircle(
//                color = Color.White,
//                radius = radius,
//                style = style,
//                center = Offset(x.toFloat(), y.toFloat())
//            )
        }
    }
}


