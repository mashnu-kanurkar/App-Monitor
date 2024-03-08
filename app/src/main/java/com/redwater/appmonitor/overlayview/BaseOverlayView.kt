package com.redwater.appmonitor.overlayview

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.redwater.appmonitor.R
import com.redwater.appmonitor.advertising.ADManager
import com.redwater.appmonitor.advertising.BannerPlacement
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseOverlayView(context: Context,
                               attrs: AttributeSet? = null,
                               defStyleAttr: Int = 0,): LinearLayout(context, attrs, defStyleAttr) {

    private val TAG = this::class.simpleName
    protected var mOverlayViewActionListener: IOverlayViewActionListener? = null
    protected val scope = CoroutineScope(Dispatchers.Default)

    abstract fun inflateLayout()

    abstract fun parseOverlayData(overlayPayloadData: String)

    fun setPackageIcon(appPackageName: String){
        try {
            val icon = context.packageManager.getApplicationIcon(appPackageName)
            findViewById<ImageView>(R.id.app_icon).setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.d(TAG, "unable to set app icon")
            e.printStackTrace()
        }
    }

    fun setBannerAD(adLayout: FrameLayout, bannerSize: ISBannerSize){
        try {
            //application context can not be cast to activity
            val banner = IronSource.createBanner(context.applicationContext as Activity?, bannerSize)
            adLayout.addView(banner)
            ADManager.getInstance(context).setBannerLayout(banner, BannerPlacement.OverlayBanner())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try{
            scope.cancel()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun setOverlayActionListener(overlayViewActionListener: IOverlayViewActionListener){
        mOverlayViewActionListener = overlayViewActionListener
    }
}