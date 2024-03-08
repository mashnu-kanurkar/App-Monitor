package com.redwater.appmonitor.advertising

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ironsource.adqualitysdk.sdk.BuildConfig
import com.ironsource.adqualitysdk.sdk.ISAdQualityConfig
import com.ironsource.adqualitysdk.sdk.ISAdQualityLogLevel
import com.ironsource.adqualitysdk.sdk.IronSourceAdQuality
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ADManager private constructor(private val context: Context,): LevelPlayInterstitialListener, DefaultLifecycleObserver{

    private val TAG = this::class.simpleName
    private val IRONSOURCE_KEY = "1d5b3e885"
    private lateinit var advertisingId: String
    private lateinit var bannerLayout: IronSourceBannerLayout
    private lateinit var interstitialCapper: Job
    private var isCapperEnabled = false
    private var currentBanner: BannerPlacement? = null
    companion object{
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ADManager? = null

        fun getInstance(context: Context): ADManager{
            Logger.d("ADManager", "ADManager instance requested")
            return instance?: synchronized(this){
                ADManager(context.applicationContext)
            }
        }
    }
    fun initialiseIronSource(){
        val adQualityConfigBuilder = ISAdQualityConfig.Builder()
        adQualityConfigBuilder.setTestMode(true)
        adQualityConfigBuilder.setLogLevel(ISAdQualityLogLevel.INFO)
        val adQualityConfig = adQualityConfigBuilder.build()
        IronSourceAdQuality.getInstance().initialize(context.applicationContext, IRONSOURCE_KEY, adQualityConfig)
        advertisingId = IronSource.getAdvertiserId(context.applicationContext)
        // we're using an advertisingId as the 'userId'
        Logger.d(TAG, "AD ID $advertisingId")
        if (BuildConfig.DEBUG){
            Logger.d(TAG, "Build config isDebug ${BuildConfig.DEBUG}")
            enableTestSuit(true)
        }else{
            enableTestSuit(false)
        }
        IronSource.setLevelPlayInterstitialListener(this)
        IronSource.setUserId(advertisingId)
        IronSource.init(context, IRONSOURCE_KEY){
            Logger.d(TAG, "IronSource init completed")
            Logger.d(TAG, "AD ID 2: $advertisingId")
            //if (isTest) IronSource.launchTestSuite(context);

        }
        IronSource.loadInterstitial()
    }

    override fun onStart(owner: LifecycleOwner) {
        Logger.d(TAG, "lifecycle owner onStart ${owner.lifecycle.currentState}, setting isCapperEnabled to true")
        isCapperEnabled = true
    }

    override fun onStop(owner: LifecycleOwner) {
        Logger.d(TAG, "lifecycle owner onStop ${owner.lifecycle.currentState}, setting isCapperEnabled to false")
        isCapperEnabled = false
        if (this::interstitialCapper.isInitialized){
            interstitialCapper.cancel()
        }
    }



    private fun enableTestSuit(enable: Boolean = false){
        if (enable){
            IronSource.setMetaData("is_test_suite", "enable")
        }
    }

    fun setBannerLayout(ironSourceBannerLayout: IronSourceBannerLayout, placement: BannerPlacement){
        if (currentBanner == null){
            Logger.d(TAG, "Current banner: $currentBanner vs placement requested: $placement")
            currentBanner = placement
            bannerLayout = ironSourceBannerLayout
            currentBanner?.let{
                loadNewBanner(it)
            }
        }else{
            if (currentBanner != placement){
                Logger.d(TAG, "Current banner: $currentBanner vs placement requested: $placement")
                bannerLayout = ironSourceBannerLayout
                currentBanner?.let {
                    loadNewBanner(it)
                }
            }
        }

    }

    private fun loadNewBanner(placement: BannerPlacement){
        Logger.d(TAG, "Loading new banner")
        bannerLayout.levelPlayBannerListener = object : LevelPlayBannerListener {
            override fun onAdLoaded(p0: AdInfo?) {
                Logger.d(TAG, "Banner onAdLoaded")
                bannerLayout.visibility = View.VISIBLE
            }

            override fun onAdLoadFailed(p0: IronSourceError?) {
                Logger.d(TAG, "Banner onAdLoadFailed")
            }

            override fun onAdClicked(p0: AdInfo?) {
                Logger.d(TAG, "Banner onAdClicked")
            }

            override fun onAdLeftApplication(p0: AdInfo?) {
                Logger.d(TAG, "Banner onAdLeftApplication")
            }

            override fun onAdScreenPresented(p0: AdInfo?) {
                Logger.d(TAG, "Banner onAdScreenPresented")
            }

            override fun onAdScreenDismissed(p0: AdInfo?) {
                Logger.d(TAG, "Banner onAdScreenDismissed")
            }

        }
        Logger.d(TAG, "Banner initialised inner: ${this::bannerLayout.isInitialized}")
        Logger.d("banner", "will try to load banner")
        IronSource.loadBanner(bannerLayout, placement.name)
    }

    override fun onAdReady(p0: AdInfo?) {
        Logger.d(TAG, "Interstitial onAdReady")
    }

    override fun onAdLoadFailed(p0: IronSourceError?) {
        Logger.d(TAG, "Interstitial onAdLoadFailed")
        IronSource.loadInterstitial()
    }

    override fun onAdOpened(p0: AdInfo?) {
        Logger.d(TAG, "Interstitial onAdOpened")
    }

    override fun onAdShowSucceeded(p0: AdInfo?) {
        Logger.d(TAG, "Interstitial onAdShowSucceeded")
    }

    override fun onAdShowFailed(p0: IronSourceError?, p1: AdInfo?) {
        Logger.d(TAG, "Interstitial onAdShowFailed")
        IronSource.loadInterstitial()
    }

    override fun onAdClicked(p0: AdInfo?) {
        Logger.d(TAG, "Interstitial onAdClicked")
    }

    override fun onAdClosed(p0: AdInfo?) {
        Logger.d(TAG, "Interstitial onAdClosed")
        delayNextInterstitial()
    }

    private fun delayNextInterstitial(timeInMin: Int = 3){
        if (isCapperEnabled){
            Logger.d(TAG, "isCapperEnabled $isCapperEnabled, delaying next interstitial")
            interstitialCapper = CoroutineScope(Dispatchers.Default).launch {
                delay((timeInMin*60*1000).toLong())
                IronSource.loadInterstitial()
            }
        }else{
            Logger.d(TAG, "isCapperEnabled $isCapperEnabled, will try to cancel interstitialCapper")
            if (this::interstitialCapper.isInitialized){
                Logger.d(TAG, "isCapperEnabled $isCapperEnabled, will try to cancel interstitialCapper")
                interstitialCapper.cancel()
            }else{
                Logger.d(TAG, "isCapperEnabled $isCapperEnabled, interstitialCapper not initialised")
            }
        }
    }

}

sealed class BannerPlacement(val name: String){
    class HomeBanner() : BannerPlacement("Home")
    class OverlayBanner(): BannerPlacement("Overlay")
}