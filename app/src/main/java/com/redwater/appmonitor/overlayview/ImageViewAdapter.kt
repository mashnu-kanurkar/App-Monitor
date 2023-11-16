package com.redwater.appmonitor.overlayview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.model.ImageOption

class ImageViewAdapter(val context: Context, private val imageOptionList: List<ImageOption>, private val overlayViewActionListener: OverlayViewActionListener? = null): BaseAdapter() {

    private val inflater:LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int {
        return imageOptionList.size
    }

    override fun getItem(position: Int): Any {
        return imageOptionList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.image_options_layout, null)
        val imageView = view.findViewById<ImageView>(R.id.option_image)
        Glide.with(context)
            .load(imageOptionList[position].imageUrl)
            .centerInside()
            .placeholder(R.drawable.loading_spinner)
            .into(imageView)

        imageView.setOnClickListener {
            if (imageOptionList[position].isAnswer){
                overlayViewActionListener?.onOpenAppAction()
            }else{
                overlayViewActionListener?.onCloseAppAction()
            }
        }
        return view
    }
}