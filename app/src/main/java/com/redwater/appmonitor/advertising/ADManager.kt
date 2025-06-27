package com.redwater.appmonitor.advertising

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ironsource.adqualitysdk.sdk.ISAdQualityConfig
import com.ironsource.adqualitysdk.sdk.ISAdQualityLogLevel
import com.ironsource.adqualitysdk.sdk.IronSourceAdQuality
import com.ironsource.mediationsdk.IronSource
import com.redwater.appmonitor.BuildConfig
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.logger.Logger
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayAdSize
import com.unity3d.mediation.LevelPlayInitRequest
import com.unity3d.mediation.banner.LevelPlayBannerAdView
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd

class ADManager private constructor(private val context: Context): ActivityListener, DefaultLifecycleObserver{

    private val TAG = this::class.simpleName
    private var bannerParentLayout: FrameLayout? = null
    private var bannerAd : LevelPlayBannerAdView? = null
    private var mInterstitialAd: LevelPlayInterstitialAd? = null
    private val appKey = BuildConfig.IRON_SOURCE_APP_KEY


    companion object{
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ADManager? = null

        fun getInstance(context: Context): ADManager {
            Logger.d("ADManager", "ADManager instance requested")
            return instance ?: synchronized(this) {
                val created = ADManager(context)
                instance = created // <-- ADD THIS LINE
                created
            }
        }
    }

    fun initialiseIronSource(bannerLayout: FrameLayout){
        Logger.d(TAG, "initialiseIronSource $appKey")
        bannerParentLayout = bannerLayout


        val adQualityConfigBuilder = ISAdQualityConfig.Builder()
            .setTestMode(false)
            .setLogLevel(ISAdQualityLogLevel.VERBOSE)

        val adQualityConfig = adQualityConfigBuilder.build()
        IronSourceAdQuality.getInstance().initialize(context, appKey, adQualityConfig)

        if (BuildConfig.DEBUG){
            Logger.d(TAG, "Build config isDebug ${BuildConfig.DEBUG}")
            enableTestSuit(true)
            LevelPlay.validateIntegration(context)
        }else{
            enableTestSuit(false)
        }
        initLevelPlay()
    }

    private fun initLevelPlay() {
        Logger.d(TAG, "initialising LevelPlay")


        val legacyFormats = listOf(LevelPlay.AdFormat.INTERSTITIAL, LevelPlay.AdFormat.BANNER)
        val initRequest = LevelPlayInitRequest.Builder(appKey)
            .withLegacyAdFormats(legacyFormats)
            .build()
        LevelPlay.init(context, initRequest, InitializationListener(this))
        Logger.d(TAG, "LevelPlay initialisation complete")
    }


    private fun enableTestSuit(enable: Boolean = false){
        if (enable){
            LevelPlay.setMetaData("is_test_suite", "enable")
        }
    }

    override fun loadInterstitial() {
        mInterstitialAd?.loadAd()
    }

    override fun loadBanner() {
        bannerAd?.loadAd()
    }

    fun showInterstitialAd(activity: Activity) {
        Logger.d(TAG, "Showing interstitial")
        mInterstitialAd?.let {
            if (it.isAdReady()){
                it.showAd(activity)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        Logger.d(TAG, "lifecycle owner onStart ${owner.lifecycle.currentState}, setting isCapperEnabled to true")
        bannerAd?.loadAd()
    }

    override fun onStop(owner: LifecycleOwner) {
        Logger.d(TAG, "lifecycle owner onStop ${owner.lifecycle.currentState}, setting isCapperEnabled to false")
        bannerAd?.destroy()
    }

    override fun setBannerViewVisibility(visibility: Int) {
        bannerParentLayout?.visibility = visibility
    }

    override fun createInterstitialAd() {
        mInterstitialAd = LevelPlayInterstitialAd(Constants.INTERSTITIAL_AD_UNIT_ID)
        mInterstitialAd?.setListener(InterstitialAdListener(this))
        loadInterstitial()
    }

    override fun createBannerAd() {
        // choose banner size
        // 1. recommended - Adaptive ad size that adjusts to the screen width
        val adSize = LevelPlayAdSize.createAdaptiveAdSize(context)

        // 2. Adaptive ad size using fixed width ad size
        //  val  adSize = LevelPlayAdSize.createAdaptiveAdSize(this, 400)

        // 3. Specific banner size - BANNER, LARGE, MEDIUM_RECTANGLE
        // val adSize = LevelPlayAdSize.BANNER

        // Create the banner view and set the ad unit id and ad size
        adSize?.let {
            val config = LevelPlayBannerAdView.Config.Builder().setAdSize(it).build()
            bannerAd = LevelPlayBannerAdView(context, Constants.BANNER_AD_UNIT_ID, config)

            // set the banner listener
            bannerAd?.setBannerListener(BannerAdListener(this))

            // add LevelPlayBannerAdView to your container
            val layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            bannerParentLayout?.addView(bannerAd, 0, layoutParams)

        }?: run {
            Logger.d(TAG, "Failed to create banner ad")
        }
        loadBanner()
    }

    override fun launchTestSuit() {
        Logger.d(TAG, "Launch test suit: ${BuildConfig.DEBUG}")
        if (BuildConfig.DEBUG){

            LevelPlay.launchTestSuite(context)
        }
    }

}

sealed class BannerPlacement(val name: String){
    class HomeBanner() : BannerPlacement("Home")
    class OverlayBanner(): BannerPlacement("Overlay")
}