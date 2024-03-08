package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RWHeaderText(modifier: Modifier = Modifier,
                 text: String,
                 textAlign: TextAlign = TextAlign.Center,
                 color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
                 isError: Boolean = false) {
    Text(modifier = modifier.padding(2.dp, 4.dp), text = text,
        textAlign = textAlign,
        color = if (isError) MaterialTheme.colorScheme.onError else color,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
}

@Composable
fun RWBodyText(modifier: Modifier = Modifier, text: String, textAlign: TextAlign = TextAlign.Center,  color: Color = MaterialTheme.colorScheme.onSurfaceVariant, isError: Boolean = false) {
    Text(modifier = modifier.padding(2.dp, 4.dp),
        text = text,
        textAlign = textAlign,
        color = if (isError) MaterialTheme.colorScheme.onError else color,
        style = MaterialTheme.typography.bodyLarge)
}

@Composable
fun RWItalicText(modifier: Modifier = Modifier, text: String, textAlign: TextAlign = TextAlign.Center, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, isError: Boolean = false) {
    Text(modifier = modifier.padding(2.dp, 4.dp), text = text,
        textAlign = textAlign,
        color = if (isError) MaterialTheme.colorScheme.onError else color,
        style = TextStyle(fontStyle = FontStyle.Italic))
}

@Composable
fun RWUnderlineText(modifier: Modifier = Modifier, text: String, textAlign: TextAlign = TextAlign.Center, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, isError: Boolean = false) {
    Text(modifier = modifier.padding(2.dp, 4.dp), text = text,
        textAlign = textAlign,
        color = if (isError) MaterialTheme.colorScheme.onError else color,
        style =  TextStyle(
        fontWeight = FontWeight.Medium,
        textDecoration = TextDecoration.Underline
    ))
}

@Composable
fun RWBigDisplayText(modifier: Modifier = Modifier, text: String, textAlign: TextAlign = TextAlign.Center, color: Color = MaterialTheme.colorScheme.onSurfaceVariant,  isError: Boolean = false) {
    Text(modifier = modifier.padding(2.dp, 4.dp), text = text, textAlign = textAlign,
        color = if (isError) MaterialTheme.colorScheme.onError else color,
        style = MaterialTheme.typography.displaySmall)
}