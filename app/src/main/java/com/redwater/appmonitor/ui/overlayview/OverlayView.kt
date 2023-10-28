package com.redwater.appmonitor.ui.overlayview

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

abstract class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr)  {

}