package com.redwater.appmonitor.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    analyticsViewModel: AnalyticsViewModel,
    packageName: String?,
    context: Context = LocalContext.current, ) {

    val TAG = "AnalyticsScreen"
    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween){
        Card(shape = RoundedCornerShape(8.dp)) {
            if (packageName != null) {
                Text(text = packageName)
            }
        }
    }

}