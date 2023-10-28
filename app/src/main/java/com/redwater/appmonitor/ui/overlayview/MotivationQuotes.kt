package com.redwater.appmonitor.ui.overlayview

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MotivationQuotes(
    appIcon: Drawable?,
    quoteOrImageUrl: String,
    onButtonClick: (Boolean)->Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Image(modifier = Modifier
//            .size(48.dp)
//            .padding(8.dp),
//            bitmap = appIcon?.toBitmap(48, 48)?.asImageBitmap()?:
//            context.packageManager.getApplicationIcon(context.packageName).toBitmap(48, 48).asImageBitmap(),
//            contentDescription = "app icon")

        if (quoteOrImageUrl.startsWith("http")){
            GlideImage(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
                contentScale = ContentScale.FillBounds,
                model = quoteOrImageUrl, contentDescription = "motivation quotes image")
        }else{
            Text(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), text = quoteOrImageUrl, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Preview
@Composable
fun MotivationQuotesPreview() {
    MotivationQuotes(appIcon = null, quoteOrImageUrl = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg", onButtonClick = {})
}