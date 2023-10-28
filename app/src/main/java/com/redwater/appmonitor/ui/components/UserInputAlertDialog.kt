package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TimeSelectionDialog(title: String,
                        description: String,
                        timeList: List<String>,
                        onSelection: (Int)->Unit,
                        onDismiss: ()-> Unit,
) {
    var selectedIndex by remember {
        mutableStateOf(0)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(onClick = {onSelection.invoke(selectedIndex)}) {
                Text(text = "Done")
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
                UserSelectionExposedDropdownMenuBox(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                    itemList = timeList,
                    selectedIndex = selectedIndex,
                    onSelectionChanged ={index -> selectedIndex = index}
                )
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectionExposedDropdownMenuBox(
    modifier: Modifier = Modifier,
    itemList: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = itemList[selectedIndex],
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                itemList.forEachIndexed{ index, item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            onSelectionChanged.invoke(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

