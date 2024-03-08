package com.redwater.appmonitor.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.utils.TimeFormatUtility
import com.redwater.appmonitor.utils.getColorsForPieChart

@OptIn(ExperimentalTextApi::class)
@Composable
fun PieChart(
    data: Map<String, Int>,
    radiusOuter: Dp = 144.dp,
    chartBarWidth: Dp = 32.dp,
    animDuration: Int = 1000,
) {

    Logger.d("PieChart", "data ${data.toString()}")
    val totalSum = data.values.sum()
    val floatValue = mutableListOf<Float>()
    val keyList = data.keys.toList()
    val valuesBreakdown = data.values.map { (it*100)/totalSum }
    val totalTimeString = "Total usage\n${TimeFormatUtility().getFormattedTimeString(totalSum.toShort())}"

    // To set the value of each Arc according to
    // the value given in the data, we have used a simple formula.
    // For a detailed explanation check out the Medium Article.
    // The link is in the about section and readme file of this GitHub Repository
    data.values.forEachIndexed { index, values ->
        floatValue.add(index, 360 * values.toFloat() / totalSum.toFloat())
    }

    var animationPlayed by remember { mutableStateOf(false) }
    val generatedColors = getColorsForPieChart()

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
//    val animateRotation by animateFloatAsState(
//        targetValue = if (animationPlayed) 360f else 0f,
//        animationSpec = tween(
//            durationMillis = animDuration,
//            delayMillis = 0,
//            easing = LinearOutSlowInEasing
//        ), label = "PieChart rotate Animation"
//    )

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
                    //.rotate(animateRotation)

            ) {
                // draw each Arc for each data entry in Pie Chart
                floatValue.forEachIndexed { index, value ->
                    drawArc(
                        color = generatedColors[index],
                        lastValue,
                        value,
                        useCenter = true,
                        //style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    lastValue += value
                }

                drawCircle(color = Color.Gray, radius = (size.minDimension / 2.0f) - chartBarWidth.toPx())
                val measuredText = textMeasurer.measure(
                    text = totalTimeString,
                    style = TextStyle(fontWeight = FontWeight.Bold,),
                    maxLines = 2
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = totalTimeString,
                    topLeft = Offset(x = (size.width/2)-(measuredText.size.width/2), y=(size.height/2)-(measuredText.size.height/2))
                )
            }
        }

        for (index in keyList.indices step 2){
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start) {
                ChartLegend(legendName = keyList.get(index),
                    value = valuesBreakdown.get(index),
                    color = generatedColors.get(index))
                if ((index + 1) < keyList.size){
                    ChartLegend(legendName = keyList.get(index+1),
                        value = valuesBreakdown.get(index+1),
                         color = generatedColors.get(index+1))
                }

            }
        }

    }

}

@Composable
fun RowScope.ChartLegend(legendName: String, value: Int, color:Color) {
    Row(modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth()
        .weight(1f),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "$legendName ($value%)")
    }
}
