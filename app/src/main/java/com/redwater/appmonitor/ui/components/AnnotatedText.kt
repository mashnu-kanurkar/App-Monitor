package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.redwater.appmonitor.logger.Logger

@Composable
fun AnnotatedClickableText(normalTextPart: String, clickableTextPart: String, url: String) {
    val annotatedText = buildAnnotatedString {
        append(normalTextPart)

        // We attach this *URL* annotation to the following content
        // until `pop()` is called
        pushStringAnnotation(
            tag = "URL", annotation = url
        )
        withStyle(
            style = SpanStyle(
                color = Color.Blue, fontWeight = FontWeight.Medium
            )
        ) {
            append(clickableTextPart)
        }
        pop()
    }
    val uriHandler = LocalUriHandler.current
    ClickableText(text = annotatedText, onClick = { offset ->
        // We check if there is an *URL* annotation attached to the text
        // at the clicked position
        annotatedText.getStringAnnotations(
            tag = "URL", start = offset, end = offset
        ).firstOrNull()?.let { annotation ->
            // If yes, we log its value
            Logger.d("Clicked URL", annotation.item)
            uriHandler.openUri(annotation.item)
        }
    })
}

@Preview
@Composable
fun AnnotatedTextPreview() {
    AnnotatedClickableText(normalTextPart = "Read ", clickableTextPart = "Privacy Policy", url = "")
}