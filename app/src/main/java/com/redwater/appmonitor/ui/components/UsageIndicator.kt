package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UsageIndicator(usageTimeInMin: Float, thresholdTimeInMin: Float?) {
    val dummyThreshold: Float = (24*60).toFloat()

    val usageIndicatorValue = usageTimeInMin/(thresholdTimeInMin?:dummyThreshold)
    val usagePercentage = if (usageIndicatorValue < 1.0 ) usageIndicatorValue else 1.0
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp, 2.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(modifier = Modifier
            .padding(8.dp, 2.dp)
            .weight(1f), progress = usagePercentage.toFloat())
        thresholdTimeInMin?.let {
            Text(text = "Limit: $it Min", style = MaterialTheme.typography.labelSmall)
        }
    }
}