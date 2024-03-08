package com.redwater.appmonitor.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource

@Composable
fun AdvertView(modifier: Modifier = Modifier, activity: Activity) {
    val isInEditMode = LocalInspectionMode.current
    if (isInEditMode) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            text = "Advert Here",
        )
    } else {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                val banner = IronSource.createBanner(activity, ISBannerSize.BANNER)
                banner
            },
            update = {

            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertPreview() {
    //AdvertView()
}