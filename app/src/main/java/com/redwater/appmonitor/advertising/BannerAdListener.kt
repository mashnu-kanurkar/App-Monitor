package com.redwater.appmonitor.advertising

import android.view.View
import com.redwater.appmonitor.logger.Logger
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener

class BannerAdListener(private val listener: ActivityListener) :
    LevelPlayBannerAdViewListener {

    private val TAG = BannerAdListener::class.java.name

    /**
    Called after each banner ad has been successfully loaded, either a manual load or banner refresh
    @param adInfo The info of the ad
     */
    override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
        listener.setBannerViewVisibility(View.VISIBLE)
    }

    /**
    Called after a banner has attempted to load an ad but failed
    This delegate will be sent both for manual load and refreshed banner failures
    @param error The reason for the error
     */
    override fun onAdLoadFailed(error: LevelPlayAdError) {
        Logger.d(TAG, "error = $error")
        listener.loadBanner()
    }

    /**
    Called after a banner was displayed and visible on screen
    @param adInfo The info of the ad
     */
    override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

    /**
    Called after a banner failed to be displayed on screen
    @param adInfo The info of the ad
    @param error The reason for the error
     */
    override fun onAdDisplayFailed(adInfo: LevelPlayAdInfo, error: LevelPlayAdError) {
        Logger.d(TAG, "error = $error | adInfo = $adInfo")
        listener.loadBanner()
    }

    /**
    Called after a banner has been clicked
    @param adInfo The info of the ad
     */
    override fun onAdClicked(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

    /**
    Called when a banner opened on full screen
    @param adInfo The info of the ad
     */
    override fun onAdExpanded(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

    /**
    Called after a banner is restored to its original size
    @param adInfo The info of the ad
     */
    override fun onAdCollapsed(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

    /**
    Called when a user pressed on the ad and was navigated out of the app
    @param adInfo The info of the ad
     */
    override fun onAdLeftApplication(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }
}