package com.redwater.appmonitor.overlayview.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.BaseOverlayView
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class MemeView(context: Context,
               attrs: AttributeSet? = null,
               defStyleAttr: Int = 0,
               private val overlayPayloadData: String
) : BaseOverlayView(context, attrs, defStyleAttr) {

    private val TAG = this::class.simpleName
    private var timeLimit = 5 //5 sec
    private lateinit var buttonPositive : Button
    private lateinit var buttonNegative: Button

    init {
        inflateLayout()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parseOverlayData(overlayPayloadData)
        initiateTime()
    }

    override fun inflateLayout() {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        inflate(context, R.layout.overlay_meme_layout, this)
    }

    override fun parseOverlayData(overlayPayloadData: String) {
        Logger.d(TAG,"overlay payload to parse $overlayPayloadData")
        var data = JSONObject(Constants.defMeme)
        try{
            data = JSONObject(overlayPayloadData)
            mOverlayViewActionListener?.onRendered(true)
        }catch (e: Exception){
            e.printStackTrace()
            mOverlayViewActionListener?.onRendered(false)
        }
        Logger.d(TAG, "overlay data $data")
        val memeUrl = data.getString("image")

        val memeImage = findViewById<ImageView>(R.id.meme_image)
        Glide.with(context)
            .load(memeUrl)
            .centerInside()
            .placeholder(R.drawable.loading_spinner)
            .into(memeImage)
        buttonPositive = findViewById<Button>(R.id.meme_button_positive)
        buttonPositive.setOnClickListener {
            mOverlayViewActionListener?.onCloseAppAction()
        }
        buttonNegative = findViewById(R.id.meme_button_negative)
        buttonNegative.setOnClickListener {
            mOverlayViewActionListener?.onOpenAppAction()
        }

    }

    private fun initiateTime(){
        scope.launch {
            if (isActive){
                while (timeLimit >= 0){
                    buttonNegative.visibility = VISIBLE
                    timeLimit -= 1
                    delay(1000)
                    if (timeLimit < 0){
                        scope.cancel()
                    }
                }
            }
        }
    }

}