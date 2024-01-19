package com.redwater.appmonitor.overlayview.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.createHorizontalAxis
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
import com.redwater.appmonitor.utils.VicoChartUtility
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
        val isDelayOptionAvailable = data.getString("isDelayOptionAvbl").toBoolean()
        val limitText = findViewById<TextView>(R.id.text_set_limit)
        val usageText = findViewById<TextView>(R.id.text_total_usage)

        limitText.text = limitText.text.toString().replace("##limit##", limit)
        usageText.text = usageText.text.toString().replace("##usage##", usage)

        findViewById<Button>(R.id.button_close).setOnClickListener {
            mOverlayViewActionListener?.onDismissOverlayAction(delayInMin = 0)
        }

        findViewById<Button>(R.id.button_delay).apply {
            if (isDelayOptionAvailable.not()){
                isEnabled = false
                text = "Delay not available"
            }else{
                text = text.toString().replace("##delayMin##", "10")
                setOnClickListener {
                    mOverlayViewActionListener?.onDismissOverlayAction(delayInMin = 10)
                }
            }

        }


    }

    fun plotChart(usageDist: Map<Short, Long>, thresholdValue: Short){
        Logger.d(TAG, "Plotting chart with values $usageDist")
        val entries = VicoChartUtility().getEntriesWithLabel(usageDist = usageDist)
        val chartView = findViewById<ChartView>(R.id.chart_view)
        chartView.entryProducer = ChartEntryModelProducer(entries.first)
        val horizontalValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            entries.second[value.toInt()]
        }
        chartView.chart?.apply {
            createHorizontalAxis<AxisPosition.Horizontal.Bottom> {
                valueFormatter = horizontalValueFormatter
            }
        }
    }

}