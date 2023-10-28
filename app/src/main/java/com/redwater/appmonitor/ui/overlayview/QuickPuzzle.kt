package com.redwater.appmonitor.ui.overlayview

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.redwater.appmonitor.R
import com.redwater.appmonitor.ui.models.HintText
import com.redwater.appmonitor.utils.hexStringToColorValue


@Composable
fun QuickPuzzle(appIcon: Drawable?,
                remainingTime: Float,
                puzzle: String,
                hintList: List<HintText>,
                onClickHintText: (Int)->Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(modifier = Modifier
            .size(48.dp)
            .padding(8.dp),
            bitmap = appIcon?.toBitmap(48, 48)?.asImageBitmap()?:
            context.packageManager.getApplicationIcon(context.packageName).toBitmap(48, 48).asImageBitmap(),
            contentDescription = "app icon")

        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), text = stringResource(id = R.string.quick_puzzle_title))
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            progress = remainingTime)
        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), text = puzzle)
        LazyVerticalGrid(columns = GridCells.Fixed(2),){
            itemsIndexed(hintList){index, hintText->
                val color = Color.Unspecified
                try{
                    hintText.textColor?.let {
                        hexStringToColorValue(it)
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
                Text(modifier = Modifier.padding(8.dp)
                    .clickable {
                          onClickHintText.invoke(index)
                },
                    text = hintText.text,
                    color = color)
            }
        }
    }
}

