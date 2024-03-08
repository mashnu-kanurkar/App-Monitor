package com.redwater.appmonitor.overlayview.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.MainActivity
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.BaseOverlayView
import org.json.JSONObject

class DNDOverlayView(context: Context,
                     attrs: AttributeSet? = null,
                     defStyleAttr: Int = 0,
                     private val overlayPayloadData: String): BaseOverlayView(context, attrs, defStyleAttr) {

    private val TAG = this::class.simpleName
    init {
        inflateLayout()
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parseOverlayData(overlayPayloadData)
    }
    override fun inflateLayout() {
        inflate(context, R.layout.dnd_overlay_layout, this)
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
        //val dndStart = data.getString("dndStart")
        val dndEnd = data.getString("dndEnd")
        val dndText = findViewById<TextView>(R.id.text_dnd_usage_title)

        dndText.text = dndText.text.toString().replace("##dndEndTime##", dndEnd)


        findViewById<Button>(R.id.button_close).setOnClickListener {
            mOverlayViewActionListener?.onDismissOverlayAction(delayInMin = 0)
        }

        findViewById<Button>(R.id.button_change_dnd).apply {
            setOnClickListener {
                mOverlayViewActionListener?.onDismissOverlayAction(delayInMin = 0)
                val intent = Intent(context.applicationContext, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

}