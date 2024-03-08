package com.redwater.appmonitor.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.ui.components.AnnotatedClickableText
import com.redwater.appmonitor.ui.components.PieChart
import com.redwater.appmonitor.ui.components.RWBodyText
import com.redwater.appmonitor.ui.components.RWHeaderText
import com.redwater.appmonitor.ui.components.RWItalicText
import com.redwater.appmonitor.ui.components.RWUnderlineText
import com.redwater.appmonitor.viewmodel.DashboardViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Dashboard(modifier: Modifier = Modifier,
              lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
              dashboardViewModel: DashboardViewModel,
              context: Context = LocalContext.current,
              onNavigateToAppListing: ()->Unit,
              onNavigateToDND: ()-> Unit) {
    val TAG = "DashboardScreen"
    val quote by dashboardViewModel.quote
    val appData = dashboardViewModel.appData.toMap()
    val blog by dashboardViewModel.blog

    DisposableEffect(key1 = LocalLifecycleOwner.current,){
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                Logger.d(TAG, "on create")
                dashboardViewModel.getUsageDataForChart(context = context)
                dashboardViewModel.getQuote()
                dashboardViewModel.getBlog()
            }
        }
        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)
        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        //Quotes card
        quote?.let {
            Card(modifier = Modifier
                .padding(8.dp, 16.dp)
                .fillMaxWidth(),) {
                RWHeaderText(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.quote_title),)
                Divider(Modifier.fillMaxWidth().height(2.dp).background(color = Color.Black))
                RWItalicText(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    text = it.text,
                    textAlign = TextAlign.Center,)
                RWItalicText(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    text = it.author,
                    textAlign = TextAlign.End,)
            }
        }

        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            Column(modifier = modifier.weight(1f)) {
                Card(modifier = Modifier
                    .padding(8.dp, 16.dp, 8.dp, 1.dp)
                    .weight(1f)
                    .fillMaxHeight(),) {
                    RWHeaderText(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        text = stringResource(id = R.string.usage_limit_header))
                    Divider(Modifier.fillMaxWidth().height(2.dp).background(color = Color.Black))
                    RWBodyText(modifier = Modifier
                        .padding(8.dp),
                        text = stringResource(id = R.string.usage_limit_description),
                        textAlign = TextAlign.Start,)
                }
                ElevatedButton(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 1.dp), onClick = onNavigateToAppListing) {
                    RWHeaderText(text = stringResource(id = R.string.usage_limit_button),)
                }
            }
            Column(modifier = modifier.weight(1f)) {
                Card(modifier = Modifier
                    .padding(8.dp, 16.dp, 8.dp, 1.dp)
                    .weight(1f)
                    .fillMaxHeight()
                    ,) {
                    RWHeaderText(modifier = Modifier
                        .padding(8.dp),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.dnd_header))
                    Divider(Modifier.fillMaxWidth().height(2.dp).background(color = Color.Black))
                    RWBodyText(modifier = Modifier
                        .padding(8.dp),
                        text = stringResource(id = R.string.dnd_description),
                        textAlign = TextAlign.Center,)

                }
                ElevatedButton(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 1.dp), onClick = onNavigateToDND) {
                    RWHeaderText(text = stringResource(id = R.string.dnd_button))
                }
            }

        }

        Card(modifier = Modifier
            .padding(8.dp, 16.dp)
            .fillMaxWidth(),) {
            RWHeaderText(modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.app_usage_breakdown))
            Divider(Modifier.height(2.dp))
            if (appData.isEmpty()){
                CircularProgressIndicator(modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
            }else{
                if (appData.values.sum() > 0){
                    PieChart(data = appData)
                }
            }
        }
        blog?.let{
            Card(
                modifier = Modifier
                    .padding(8.dp, 16.dp)
                    .fillMaxWidth(),
            ) {
                RWHeaderText(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.blog_header),)
                Divider(Modifier.height(2.dp))
                GlideImage(model = it.imageUrl,
                    contentDescription ="",
                    modifier = Modifier.fillMaxSize())
                RWUnderlineText(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    text = it.header,
                    textAlign = TextAlign.Start,
                )
                AnnotatedClickableText(
                    modifier = Modifier.padding(8.dp), normalTextPart = it.paragraph,
                    clickableTextPart = stringResource(id = R.string.read_more),
                    url = it.blogUrl
                )
            }
        }

    }
}
@Preview(device = Devices.PIXEL_2, showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    val text = LoremIpsum(25).values.toMutableList()
    text.add(" .... ")

}
