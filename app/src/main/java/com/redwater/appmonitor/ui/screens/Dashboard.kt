package com.redwater.appmonitor.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.R
import com.redwater.appmonitor.ui.components.AnnotatedClickableText
import com.redwater.appmonitor.ui.components.PieChart

@Composable
fun Dashboard( blogTruncatedText: String) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(modifier = Modifier
            .padding(8.dp, 16.dp)
            .fillMaxWidth(),) {
            Text(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.quote_title),
                style = TextStyle(fontWeight = FontWeight.Bold))
            Divider(Modifier.height(2.dp))
            Text(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
                text = "This is a long characters daily quote, which uses entire device width",
                textAlign = TextAlign.Center,
                style = TextStyle(fontStyle = FontStyle.Italic))
            Text(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
                text = "- Author Name",
                textAlign = TextAlign.End,
                style = TextStyle(fontStyle = FontStyle.Italic))
        }

        Card(modifier = Modifier
            .padding(8.dp, 16.dp)
            .fillMaxWidth(),) {
            Text(modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.app_usage_breakdown),
                style = TextStyle(fontWeight = FontWeight.Bold))
            Divider(Modifier.height(2.dp))
            val data = mapOf<String, Int>("Insta" to 15, "FB" to 25, "Kite" to 35, "other" to 25)
            PieChart(data = data)

        }

        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            Card(modifier = Modifier
                .padding(8.dp, 16.dp)
                .weight(1f)
                .fillMaxHeight()
                .align(alignment = Alignment.CenterVertically),) {
                Text(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    text = stringResource(id = R.string.usage_limit_header),
                    style = TextStyle(fontWeight = FontWeight.Bold))
                Divider(Modifier.height(2.dp))
                Text(modifier = Modifier
                    .padding(8.dp),
                    text = stringResource(id = R.string.usage_limit_description),
                    textAlign = TextAlign.Start,
                    style = TextStyle(fontStyle = FontStyle.Italic))
                Divider(modifier = Modifier.height(IntrinsicSize.Min))
                ElevatedButton(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), onClick = {  }) {
                    Text(text = stringResource(id = R.string.usage_limit_button))
                }
            }
            Card(modifier = Modifier
                .padding(8.dp, 16.dp)
                .weight(1f)
                .fillMaxHeight()
                ,) {
                Text(modifier = Modifier
                    .padding(8.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.dnd_header),
                    style = TextStyle(fontWeight = FontWeight.Bold))
                Divider(Modifier.height(2.dp))
                Text(modifier = Modifier
                    .padding(8.dp),
                    text = stringResource(id = R.string.dnd_description),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontStyle = FontStyle.Italic))
                Divider(modifier = Modifier.height(IntrinsicSize.Min))
                ElevatedButton(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), onClick = {  }) {
                    Text(text = stringResource(id = R.string.dnd_button))
                }
            }
        }

        Card(modifier = Modifier
            .padding(8.dp, 16.dp)
            .fillMaxWidth(),) {
            Text(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.blog_header),
                style = TextStyle(fontWeight = FontWeight.Bold))
            Divider(Modifier.height(2.dp))
            Text(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
                text = "This is a title of the blog",
                textAlign = TextAlign.Start,
                style = TextStyle(fontWeight = FontWeight.Medium, textDecoration = TextDecoration.Underline))
            AnnotatedClickableText(modifier = Modifier.padding(8.dp), normalTextPart = blogTruncatedText,
                clickableTextPart = stringResource(id = R.string.read_more),
                url = "")
        }

    }
}
@Preview(device = Devices.PIXEL_2, showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    val text = LoremIpsum(25).values.toMutableList()
    text.add(" .... ")
    Dashboard(blogTruncatedText = text.toString())
}
