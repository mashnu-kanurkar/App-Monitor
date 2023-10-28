package com.redwater.appmonitor.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
//import androidx.appcompat.content.res.AppCompatResources
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.lang.Exception

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var imageViewIcon: ImageView
    private var imageTimeWaste: ImageView
    private var buttonForward: Button
    private var buttonBackward: Button
    private val TAG = "OverlayView"
    private lateinit var overlayScope: CoroutineScope
    private var _userDecisionEvent = MutableSharedFlow<UserDecisionEvent>(1)
    val userDecisionEvent = _userDecisionEvent.asSharedFlow()
    private var layoutParams : ViewGroup.LayoutParams?

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        inflate(context, R.layout.overlay, this)
        imageViewIcon = findViewById(R.id.imageView)
        buttonForward = findViewById(R.id.buttonForward)
        buttonBackward = findViewById(R.id.buttonBackward)
        imageTimeWaste = findViewById(R.id.imageTimeWaste)
        //imageTimeWaste.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.time_waste))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Logger.d("onAttachedToWindow")
        try {
            overlayScope = CoroutineScope(Dispatchers.Default)
        }catch (e: Exception){
            e.printStackTrace()
        }
        buttonForward.setOnClickListener {
            Logger.d("$TAG => on Click button forward")
            overlayScope.launch {
                _userDecisionEvent.emit(UserDecisionEvent.Forward)
                _userDecisionEvent.resetReplayCache()
            }
        }
        buttonBackward.setOnClickListener {
            Logger.d("$TAG => on Click button backward")
            overlayScope.launch {
                _userDecisionEvent.emit(UserDecisionEvent.Backward)
                _userDecisionEvent.resetReplayCache()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            overlayScope.cancel()
        }catch (e: Exception){
            e.printStackTrace()
            layoutParams = null
            System.runFinalization()
        }
    }

    fun setAppPackageName(appPackageName: String) {
        try {
            val icon = context.packageManager.getApplicationIcon(appPackageName)
            imageViewIcon.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.d("$TAG => Can not set app icon")
            e.printStackTrace()
        }
    }

}

sealed class UserDecisionEvent{
    object Forward: UserDecisionEvent()
    object Backward: UserDecisionEvent()
}