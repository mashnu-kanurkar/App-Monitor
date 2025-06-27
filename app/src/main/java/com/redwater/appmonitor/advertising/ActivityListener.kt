package com.redwater.appmonitor.advertising

interface ActivityListener {
    fun setBannerViewVisibility(visibility: Int)
    fun createInterstitialAd()
    fun createBannerAd()
    fun launchTestSuit()
    fun loadInterstitial()
    fun loadBanner()
}