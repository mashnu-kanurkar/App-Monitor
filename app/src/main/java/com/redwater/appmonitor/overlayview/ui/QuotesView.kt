package com.redwater.appmonitor.overlayview.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewStub
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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

class QuotesView(context: Context,
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
        inflate(context, R.layout.overlay_quotes_layout, this)
    }

    override fun parseOverlayData(overlayPayloadData: String) {
        Logger.d(TAG,"overlay payload to parse $overlayPayloadData")
        var data = JSONObject(Constants.defQuote)
        try{
            data = JSONObject(overlayPayloadData)
            mOverlayViewActionListener?.onRendered(true)
        }catch (e: Exception){
            e.printStackTrace()
            mOverlayViewActionListener?.onRendered(false)
        }
        Logger.d(TAG, "overlay data $data")
        var type: String = "default"
        try {
            type = data.getString("type")
        }catch (e: Exception){
            e.printStackTrace()
        }
        val quotesStub = findViewById<ViewStub>(R.id.quotes_viewstub)

        if (type == "image"){
            var quoteImageUrl: String = ""
            try {
                quoteImageUrl =data.getString("image")
            }catch (e: Exception){
                e.printStackTrace()
            }
            quotesStub.layoutResource = R.layout.image_options_layout
            quotesStub.inflate()
            val quoteImageView = findViewById<ImageView>(R.id.option_image)
            Glide.with(context)
                .load(quoteImageUrl)
                .centerInside()
                .placeholder(R.drawable.loading_spinner)
                .into(quoteImageView)
        }else if (type == "text"){
            var quoteText = ""
            var quoteColor = ""
            try {
                quoteText = data.getString("text")
                quoteColor = data.getString("color")
            }catch (e: Exception){
                e.printStackTrace()
            }
            quotesStub.layoutResource = R.layout.text_options_layout
            quotesStub.inflate()
            val quoteTextView = findViewById<TextView>(R.id.option_text)
            quoteTextView.text = quoteText
            quoteTextView.setTextColor(Color.parseColor(quoteColor))
        }

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