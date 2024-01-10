package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.redwater.appmonitor.utils.VicoChartUtility
import kotlin.random.Random

@Composable
fun PermissionAlertDialog(
    title: String,
    message: String,
    onClickNo: () -> Unit,
    onClickYes: ()-> Unit,
    onDismissRequest: ()-> Unit) {
   AlertDialog(
       onDismissRequest = onDismissRequest,
       dismissButton = {
           Button(onClick = onClickNo) {
               Text(text = "No")
           }
       },
       confirmButton = {
           Button(onClick = onClickYes) {
               Text(text = "Yes")
           }
       },
       title = {
           Text(text = title, fontWeight = FontWeight.Bold,)
       },
       text = {
           Text(text = message, fontWeight = FontWeight.Normal,)
       },
       shape = RoundedCornerShape(8.dp)

   )
}