package com.redwater.appmonitor.overlayview.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.model.ImageOption
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.overlayview.BaseOverlayView
import com.redwater.appmonitor.overlayview.ImageViewAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception

class PuzzleViewImage(context: Context,
                      attrs: AttributeSet? = null,
                      defStyleAttr: Int = 0,
                      private val overlayPayloadData: String
) : BaseOverlayView(context, attrs, defStyleAttr) {
    private lateinit var timerText: TextView
    private lateinit var progressBar: ProgressBar
    private var timeLimit = 5 //5 sec
    private val TAG = this::class.simpleName

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
        inflate(context, R.layout.overlay_text_puzzle, this)
    }

    override fun parseOverlayData(overlayPayloadData: String) {
        Logger.d(TAG,"overlay payload to parse $overlayPayloadData")
        var data = JSONObject(Constants.defImagePuzzle)
        try{
            data = JSONObject(overlayPayloadData)
            mOverlayViewActionListener?.onRendered(true)
        }catch (e: Exception){
            e.printStackTrace()
            mOverlayViewActionListener?.onRendered(false)
        }
        Logger.d(TAG, "overlay data $data")
        val que = data.getString("que")
        val options = data.getJSONArray("options")

        val queImage = findViewById<ImageView>(R.id.image_puzzle_question)
        Glide.with(context)
            .load(queImage)
            .centerInside()
            .placeholder(R.drawable.loading_spinner)
            .into(queImage)

        val list = Array(options.length()) {
            options.getJSONObject(it)
        }
        val imageOptionList = mutableListOf<ImageOption>()
        val gson = Gson()
        list.forEach {optionJson->
            val imageOption = gson.fromJson(optionJson.toString(), ImageOption::class.java)
            imageOptionList.add(imageOption)
        }
        val imageViewAdapter = ImageViewAdapter(context= context,
            imageOptionList = imageOptionList,
            overlayViewActionListener = mOverlayViewActionListener)

        val gridView = findViewById<GridView>(R.id.image_puzzle_grid)
        gridView.adapter = imageViewAdapter

        timerText = findViewById<TextView>(R.id.text_puzzle_timer_info)
        progressBar = findViewById(R.id.progress)
        try {
            timeLimit = data.getString("time").toInt()
            progressBar.max = timeLimit
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun initiateTime(){
        scope.launch {
            if (isActive){
                while (timeLimit >= 0){
                    timerText.text = "Time left... $timeLimit"
                    progressBar.setProgress(timeLimit, true)
                    timeLimit -= 1
                    delay(1000)
                    if (timeLimit < 0){
                        mOverlayViewActionListener?.onCloseAppAction()
                        scope.cancel()
                    }
                }
            }
        }
    }


}