package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ErrorDescriptor(error: String, onClick:()-> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)
        .clickable {
           onClick.invoke()
    },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(Icons.Default.Close, contentDescription = "cross")
        Text(text = error, color = Color.Red, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview
@Composable
fun ErrorDescriptorPreview() {
    ErrorDescriptor(error = "No permission"){}
}