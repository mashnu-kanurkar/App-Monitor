package com.redwater.appmonitor.ui.components

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import  com.redwater.appmonitor.R
import com.redwater.appmonitor.data.model.Session
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.utils.TimeFormatUtility

@Composable
fun SessionDataCard(modifier: Modifier = Modifier, sessionData: Session?, format: String = "dd-MM-yyyy hh:mm:ss aa") {
    val timeUtility = TimeFormatUtility()
    Logger.d("Session", "${sessionData?.sessionList}")
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(8.dp,)) {
//        SessionCard(id = stringResource(id = R.string.session_id),
//            length = stringResource(id = R.string.session_length),
//            textStyle = MaterialTheme.typography.titleLarge)
//        Divider(
//            color = Color.Black,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(1.dp)
//        )
        Text(modifier = Modifier
            .padding(8.dp, 8.dp), text = stringResource(id = R.string.session_length), style = MaterialTheme.typography.titleLarge)
        sessionData?.sessionList?.forEach {
            val sessionId = timeUtility.getDateTimeFromEpoch(timestamp = it.sessionId, format= format)
            val length = timeUtility.getTimeWithSecString(timeInSec = it.sessionLength/1000)
            SessionTimeLine(id = sessionId, length = length)
            Divider(
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }

    }
}

@Composable
fun SessionTimeLine(id: String, length: String) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Spacer(modifier = Modifier
            .drawWithContent {
                drawLine(
                    color = Color.Red,
                    Offset(15.dp.toPx(), 0.dp.toPx()),
                    Offset(15.dp.toPx(), 40.dp.toPx()),
                    2f
                )
                drawContent()
                drawCircle(
                    color = Color.Red,
                    radius = 5.dp.toPx(),
                    center = Offset(15.dp.toPx(), 20.dp.toPx())
                )
            }
            .width(30.dp))
        Text(modifier = Modifier
            .weight(1.0f)
            .padding(8.dp, 8.dp), text = id, style = MaterialTheme.typography.titleMedium)
        Text(modifier = Modifier
            .weight(1.0f)
            .padding(8.dp, 8.dp), text = length, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview
@Composable
fun SessionTimeLinePreview() {
    val session = mutableMapOf<Long, Long>(1701813486000L to 4948L, 1701825665000L to 8548L)
    SessionTimeLine(id = "122", length = "2 hrs 29 min")
}


@Composable
fun SessionCard(id: String, length: String, textStyle: TextStyle = LocalTextStyle.current) {
    Row(modifier = Modifier
        .height(IntrinsicSize.Min)
        .fillMaxWidth()) {
        Text(modifier = Modifier
            .weight(1.0f)
            .padding(8.dp, 8.dp), text = id, style = textStyle)
        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
        Text(modifier = Modifier
            .weight(1.0f)
            .padding(8.dp, 8.dp), text = length, style = textStyle)
    }
}

@Preview
@Composable
fun SessionDataCardPreview() {
    val session = Session(1701813486000L, 1701825665000L)
    SessionDataCard(sessionData = session)
}