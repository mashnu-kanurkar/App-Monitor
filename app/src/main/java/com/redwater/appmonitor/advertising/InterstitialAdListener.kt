package com.redwater.appmonitor.advertising

import com.redwater.appmonitor.logger.Logger
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener

class InterstitialAdListener(private val listener: ActivityListener) :
    LevelPlayInterstitialAdListener {

    private val TAG = InterstitialAdListener::class.java.name


    /**
    Called after an interstitial ad has been loaded
    @param adInfo The info of the ad
     */
    override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

    /**
    Called after an interstitial ad has attempted to load but failed
    @param error The reason for the error
     */
    override fun onAdLoadFailed(error: LevelPlayAdError) {
        Logger.d(TAG, "error = $error")
        listener.loadInterstitial()
    }


    /**
    Called after an interstitial ad has been displayed
    This is the indication for impression
    @param adInfo The info of the ad
     */
    override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

    /**
    Called after an interstitial ad has attempted to display but failed
    @param error The reason for the error
    @param adInfo The info of the ad
     */
    override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "error = $error | adInfo = $adInfo")
        listener.loadInterstitial()
    }

    /**
    Called after an interstitial ad has been clicked
    @param adInfo The info of the ad
     */
    override fun onAdClicked(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

    /**
    Called after an interstitial ad has been closed
    @param adInfo The info of the ad
     */
    override fun onAdClosed(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
        listener.loadInterstitial()
    }

    /**
    Called after the ad info is updated. Available when another interstitial ad has loaded, and includes a higher CPM/Rate
    @param adInfo The info of the ad
     */
    override fun onAdInfoChanged(adInfo: LevelPlayAdInfo) {
        Logger.d(TAG, "adInfo = $adInfo")
    }

}