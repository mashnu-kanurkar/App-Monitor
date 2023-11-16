package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.R

@Composable
fun LoadingIndicator(message: String = "Loading data...") {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            shape = RectangleShape
        ),
        contentAlignment = Alignment.Center) {
        Row(modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 1.0f)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.padding(8.dp), text = message)
            CircularProgressIndicator(
                modifier = Modifier
                    .size(size = 64.dp)
                    .padding(8.dp),
                color = Color.Magenta,
                strokeWidth = 6.dp
            )
        }
    }
}

@Composable
fun UsageIndicator() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable(enabled = true, onClick = { }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "")
            Column(modifier = Modifier
                .padding(8.dp, 0.dp)
                .weight(2.0f, true)
            ) {
                Text(text = "name", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "packageName", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Usage time: 30 Min", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            RadioButton(selected = true,
                onClick = {
                }
            )

        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 2.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(modifier = Modifier.padding(8.dp, 2.dp).weight(1f), progress = 1f)
            Text(text = "Limit: 45 Min", style = MaterialTheme.typography.labelSmall)
        }
    }


}

@Preview
@Composable
fun UsageIndicatorPreview() {

    UsageIndicator()
}