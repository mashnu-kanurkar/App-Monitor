package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ColumnChart(columnData: Map<Any, Float>) {
    Box(modifier = Modifier
        .background(color = Color.White)
        .fillMaxWidth()
        .height(100.dp)) {
        val barCount by remember {
            mutableStateOf(columnData.entries.size)
        }
        val maxYFactor by remember {
            mutableStateOf(100f/(columnData.values.maxOrNull()?:10f))
        }

        val stepX by remember {
            mutableStateOf(20f)
        }
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp),){
            val path = Path()
            path.moveTo(0f, 0f)
//            path.lineTo(0f, size.height)
//            path.lineTo(size.width, size.height)

            columnData.entries.forEachIndexed { index, entry ->
                path.addRect(Rect(offset = Offset(stepX*(index+1), size.height), size = Size(10f, -entry.value * maxYFactor * 2)))
            }
            //path.close()
            drawPath(path, Color.Magenta, style = Stroke(width = 10f))

        }

    }
}

@Preview
@Composable
fun ChartPreview() {
    val chartData = mapOf<Any, Float>(7 to 100.0f, 8 to 100.0f, 11 to 100.0f, 15 to 100.0f)
    ColumnChart(columnData = chartData)
}