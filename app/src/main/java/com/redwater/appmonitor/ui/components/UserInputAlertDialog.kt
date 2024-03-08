package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.data.model.Period
import com.redwater.appmonitor.data.model.TimeModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionDialog(title: String,
                        description: String,
                        is24Hour: Boolean = true,
                          onSelection: (TimeModel)->Unit,
                        onDismiss: ()-> Unit,
) {
    val timeState = rememberTimePickerState(1, 30, is24Hour = is24Hour)
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(onClick = {
                if (is24Hour){
                    onSelection.invoke(TimeModel(hour = timeState.hour,
                        minute = timeState.minute,))
                }else{
                    var hour = timeState.hour
                    var period = Period.AM
                    if (timeState.hour == 12){
                        period = Period.PM
                    }else if (timeState.hour > 12){
                        hour = timeState.hour - 12
                        period = Period.PM
                    }
                    onSelection.invoke(TimeModel(hour = hour, minute = timeState.minute, period = period))
                }

            }) {
                Text(text = "Submit")
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
                TimePicker(state = timeState, layoutType = TimePickerLayoutType.Vertical)
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@Preview
@Composable
fun TimeSelectionDialogPreview() {
    TimeSelectionDialog(title = "title",
        description = "description",
        onSelection = {}) {
        
    }
}