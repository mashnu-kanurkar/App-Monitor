package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.logger.Logger

@Composable
fun PackageInfoCard(
    modifier: Modifier = Modifier,
    icon: ImageBitmap,
    name: String,
    packageName: String,
    isSelected: Boolean,
    index: Int = 0,
    usageTimeInMin: Short = 0,
    showRadioButton: Boolean = true,                
    usageIndicator:  (@Composable () -> Unit)? = null,
    onClick: (packageName: String)->Unit,
    onChangePref:(index: Int)-> Unit ) {
    //val context = LocalContext.current
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(16.dp)) {
        Row(modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Image(modifier = Modifier
                .size(48.dp)
                .padding(4.dp),
                bitmap = icon,
                contentDescription = "app icon")
            Column(modifier = Modifier
                .padding(8.dp, 0.dp)
                .weight(2.0f, true)
                .clickable(enabled = true, onClick = {
                    Logger.d("PackageInfoCard", "navigating with $packageName")
                    onClick.invoke(packageName)
                })
            ) {
                Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = packageName, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Usage time: $usageTimeInMin Min", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            if (showRadioButton){
//                RadioButton(selected = isSelected,
//                    onClick = {
//                        onChangePref.invoke(index)
//                    }
//                )
                SelectionButton(selected = isSelected, onClick = {
                    onChangePref.invoke(index)
                })
            }
        }
        if (usageIndicator != null){
            usageIndicator()
        }
        Divider()
    }
}