package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.R

@Composable
fun SelectionButton(selected: Boolean,
                    onClick: (Boolean)->Unit) {
    IconToggleButton(
        checked = selected,
        onCheckedChange = onClick,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            painter = painterResource(id = if (selected) R.drawable.baseline_check_circle_24 else R.drawable.baseline_radio_button_unchecked_24),
            contentDescription = "Radio button",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}