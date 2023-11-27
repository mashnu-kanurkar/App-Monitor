package com.redwater.appmonitor.overlayview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.model.TextOption
import com.redwater.appmonitor.logger.Logger

class TextViewAdapter(val context: Context, private val textOptionList: List<TextOption>, val overlayViewActionListener: OverlayViewActionListener? = null): BaseAdapter() {

    private val TAG = this::class.simpleName
    private val inflater:LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int {
        return textOptionList.size
    }

    override fun getItem(position: Int): Any {
        return textOptionList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.text_options_layout, parent, false)
        val textView = view.findViewById<TextView>(R.id.option_text)
        textView.text = textOptionList[position].text
        try {
            textView.setTextColor(Color.parseColor(textOptionList[position].textColor))
        }catch (e: Exception){
            e.printStackTrace()
        }
        textView.setOnClickListener {
            Logger.v(TAG, "onTextView click, ${textOptionList[position]}")
            if (textOptionList[position].isAnswer){
                overlayViewActionListener?.onOpenAppAction()
            }else{
                overlayViewActionListener?.onDismissOverlayAction()
            }
        }
        return view
    }
}