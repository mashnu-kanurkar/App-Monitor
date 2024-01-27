package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.utils.TimeFormatUtility

@Composable
fun UsageIndicator(usageTimeInMin: Float, thresholdTimeInMin: Float?) {
    val dummyThreshold: Float = (24*60).toFloat()

    val usageIndicatorValue = usageTimeInMin/(thresholdTimeInMin?:dummyThreshold)
    val usagePercentage = if (usageIndicatorValue < 1.0 ) usageIndicatorValue else 1.0
    val timeFormatUtility = remember(key1 = thresholdTimeInMin) {
        TimeFormatUtility()
    }
    val thresholdTimeString = remember(key1 = timeFormatUtility) {
        thresholdTimeInMin?.toInt()?.toShort()?.let { timeFormatUtility.getFormattedTimeString(it) }
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp, 2.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(modifier = Modifier
            .padding(8.dp, 2.dp)
            .weight(1f),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onPrimary, progress = usagePercentage.toFloat())
        thresholdTimeInMin?.let {
            Text(text = "Limit: $thresholdTimeString", style = MaterialTheme.typography.labelSmall)
        }
    }
}