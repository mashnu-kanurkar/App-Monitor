package com.redwater.appmonitor.overlayview.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import com.patrykandpatrick.vico.core.chart.addDecorations
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.views.chart.ChartView
import com.patrykandpatrick.vico.views.chart.column.columnChart
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.BaseOverlayView
import org.json.JSONObject

class BasicTimeoutView(context: Context,
                       attrs: AttributeSet? = null,
                       defStyleAttr: Int = 0,
                       private val overlayPayloadData: String
): BaseOverlayView(context, attrs, defStyleAttr) {
    private val TAG = this::class.simpleName
    init {
        inflateLayout()
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parseOverlayData(overlayPayloadData)
    }

    override fun inflateLayout() {
        inflate(context, R.layout.basic_timeout_layout, this)
    }

    override fun parseOverlayData(overlayPayloadData: String) {
        Logger.d(TAG,"overlay payload to parse $overlayPayloadData")
        var data = JSONObject(Constants.defPuzzleText)
        try{
            data = JSONObject(overlayPayloadData)
            mOverlayViewActionListener?.onRendered(true)
        }catch (e: Exception){
            e.printStackTrace()
            mOverlayViewActionListener?.onRendered(false)
        }
        Logger.d(TAG, "overlay data $data")
        val limit = data.getString("limit")
        val usage = data.getString("usage")
        val limitText = findViewById<TextView>(R.id.text_set_limit)
        val usageText = findViewById<TextView>(R.id.text_total_usage)

        limitText.text = limitText.text.toString().replace("##limit##", limit)
        usageText.text = usageText.text.toString().replace("##usage##", usage)

        findViewById<Button>(R.id.button_close).setOnClickListener {
            mOverlayViewActionListener?.onDismissOverlayAction(delayInMin = 0)
        }

        findViewById<Button>(R.id.button_delay).apply {
            text = text.toString().replace("##delayMin##", "10")
            setOnClickListener {
            mOverlayViewActionListener?.onDismissOverlayAction(delayInMin = 10)
        }
        }


    }

    fun plotChart(usageDist: Map<Short, Long>, thresholdValue: Short){
        Logger.d(TAG, "Plotting chart")

//        val chartEntryModel = entryModelOf(usageDist.toSortedMap().map { FloatEntry(it.key.toFloat(),
//            (it.value/60).toFloat()
//        ) })
        val chartEntryModel = usageDist.toSortedMap()
            .map { (x, y) -> entryOf(x.toFloat(), (y/60).toFloat()) }
            .let { entryList -> ChartEntryModelProducer(listOf(entryList)) }
            .getModel()
        val chartView = findViewById<ChartView>(R.id.chart_view)
        chartView.setModel(chartEntryModel)

        chartView.chart?.apply {
            addDecorations(listOf(ThresholdLine(thresholdValue = thresholdValue.toFloat(), thresholdLabel = thresholdValue.toString())))
        }
    }

}