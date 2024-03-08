package com.redwater.appmonitor.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.utils.getRandomColorList

@OptIn(ExperimentalTextApi::class)
@Composable
fun PieChart(
    data: Map<String, Int>,
    radiusOuter: Dp = 144.dp,
    chartBarWidth: Dp = 32.dp,
    animDuration: Int = 1000,
    textPadding: Dp = 16.dp
) {

    val totalSum = data.values.sum()
    val floatValue = mutableListOf<Float>()
    val keyList = data.keys.toList()
    val valuesBreakdown = data.values.map { (it*100)/totalSum }

    // To set the value of each Arc according to
    // the value given in the data, we have used a simple formula.
    // For a detailed explanation check out the Medium Article.
    // The link is in the about section and readme file of this GitHub Repository
    data.values.forEachIndexed { index, values ->
        floatValue.add(index, 360 * values.toFloat() / totalSum.toFloat())
    }

    var animationPlayed by remember { mutableStateOf(false) }
    var generatedColors by remember { mutableStateOf(getRandomColorList(floatValue.size)) }

    var lastValue = 0f

    // it is the diameter value of the Pie

    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) radiusOuter.value * 2f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        ), label = "PieChart size Animation"
    )

    // if you want to stabilize the Pie Chart you can use value -90f
    // 90f is used to complete 1/4 of the rotation
    val animateRotation by animateFloatAsState(
        targetValue = if (animationPlayed) 360f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        ), label = "PieChart rotate Animation"
    )

    // to play the animation only once when the function is Created or Recomposed
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pie Chart using Canvas Arc
        Box(
            modifier = Modifier
                .size(animateSize.dp),
            contentAlignment = Alignment.Center
        ) {
            val textMeasurer = rememberTextMeasurer()

            Canvas(
                modifier = Modifier
                    .offset { IntOffset.Zero }
                    .size(radiusOuter * 2f)
                    .padding(chartBarWidth)
                    .rotate(animateRotation)

            ) {
                // draw each Arc for each data entry in Pie Chart
                floatValue.forEachIndexed { index, value ->
                    drawArc(
                        color = Color(generatedColors[index]),
                        lastValue,
                        value,
                        useCenter = true,
                        //style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    lastValue += value
                }

                drawCircle(color = Color.Gray, radius = (size.minDimension / 2.0f) - chartBarWidth.toPx())
                val measuredText = textMeasurer.measure(
                    text = "World",
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "Hello",
                    topLeft = Offset(x = (size.width/2)-(measuredText.size.width/2), y=(size.height/2)-(measuredText.size.height/2))
                )
            }
        }

        for (index in keyList.indices step 2){
            Row(modifier = Modifier.width(IntrinsicSize.Max),) {
                ChartLegend(legendName = keyList.get(index), value = valuesBreakdown.get(index), color = Color(generatedColors.get(index)))
                if ((index + 1) != keyList.size){
                    ChartLegend(legendName = keyList.get(index+1), value = valuesBreakdown.get(index), color = Color(generatedColors.get(index+1)))
                }

            }
        }

    }

}

@Composable
fun RowScope.ChartLegend(legendName: String, value: Int, color:Color) {
    Row(modifier = Modifier.padding(16.dp).weight(1f)) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "$legendName ($value %)")
    }
}

@Preview(device = Devices.PIXEL_3)
@Composable
fun ChartLegendPreview() {
    val keyList = listOf<String>("Insta", "FB", "Kite", "WhatsApp", "Chrome")
    //ChartLegend(legendName = "Insta")
}