package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import com.redwater.appmonitor.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.utils.TimeFormatUtility

@Composable
fun SingleStatsCard(modifier: Modifier = Modifier, title: String, stats: String, isStatsNegative: Boolean = false) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, )
        Text(modifier = modifier, text = stats, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleLarge, color = if (isStatsNegative) MaterialTheme.colorScheme.error else Color.Unspecified)
    }
}

@Composable
fun CombinedStatsCard(modifier: Modifier = Modifier, todayUsageInMin: Short, launchCount: Int, longestSessionTimeInSec: Long, thresholdTimeInMin: Short? = null) {

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = modifier.padding(8.dp, 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                SingleStatsCard(
                    modifier = Modifier.weight(weight = 1.0f, fill = true),
                    title = stringResource(id = R.string.today_usage_title),
                    stats = TimeFormatUtility().getFormattedTimeString(todayUsageInMin),
                    isStatsNegative = if (thresholdTimeInMin != null) todayUsageInMin > thresholdTimeInMin else false
                )
                Divider(
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                SingleStatsCard(
                    modifier = Modifier.weight(weight = 1.0f, fill = true),
                    title = stringResource(id = R.string.launch_ct_title),
                    stats = launchCount.toString()
                )
            }
            Divider(
                color = Color.Black,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .height(1.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                SingleStatsCard(
                    modifier = Modifier.weight(weight = 1.0f, fill = true),
                    title = stringResource(id = R.string.longest_session_title),
                    stats = TimeFormatUtility().getTimeWithSecString(longestSessionTimeInSec)
                )
                Divider(
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                SingleStatsCard(
                    modifier = Modifier.weight(weight = 1.0f, fill = true),
                    title = stringResource(id = R.string.threshold_title),
                    stats = if (thresholdTimeInMin == null) "NA" else TimeFormatUtility().getFormattedTimeString(thresholdTimeInMin)
                )
            }
        }
    }
}

@Composable
@Preview
fun StatsCardPreview() {
     CombinedStatsCard(todayUsageInMin = 30, launchCount = 20, longestSessionTimeInSec = 2000)
}