package com.redwater.appmonitor.overlayview

import android.content.Context
import android.util.AttributeSet
import com.redwater.appmonitor.data.model.OverlayPayload
import com.redwater.appmonitor.overlayview.ui.BasicTimeoutView
import com.redwater.appmonitor.overlayview.ui.DNDOverlayView
import com.redwater.appmonitor.overlayview.ui.MemeView
import com.redwater.appmonitor.overlayview.ui.PuzzleViewImage
import com.redwater.appmonitor.overlayview.ui.PuzzleViewText
import com.redwater.appmonitor.overlayview.ui.QuotesView

class OverlayUIProvider(private val context: Context,
                        private val attrs: AttributeSet? = null,
                        private val defStyleAttr: Int = 0,
                        private var overlayPayload: OverlayPayload) {
    fun getView(): BaseOverlayView{
        return when(overlayPayload.type){
            "basic" ->{
                BasicTimeoutView(context = context, attrs = attrs, defStyleAttr = defStyleAttr, overlayPayloadData = overlayPayload.data)
            }
            "dnd" ->{
                DNDOverlayView(context = context, attrs = attrs, defStyleAttr = defStyleAttr, overlayPayloadData = overlayPayload.data)
            }
            "puzzle"-> {
                if (overlayPayload.subType == "image")
                    PuzzleViewImage(context = context, attrs = attrs, defStyleAttr = defStyleAttr, overlayPayloadData = overlayPayload.data)
                else
                    PuzzleViewText(context = context, attrs = attrs, defStyleAttr = defStyleAttr, overlayPayloadData = overlayPayload.data)
            }
            "meme"->{
                MemeView(context = context, attrs = attrs, defStyleAttr = defStyleAttr, overlayPayloadData = overlayPayload.data)
            }
            "quote"->{
                QuotesView(context = context, attrs = attrs, defStyleAttr = defStyleAttr, overlayPayloadData = overlayPayload.data)
            }
            else ->{
                PuzzleViewText(context = context, attrs = attrs, defStyleAttr = defStyleAttr, overlayPayloadData = DefaultMathPuzzle().generateQuestion())
            }
        }
    }
}