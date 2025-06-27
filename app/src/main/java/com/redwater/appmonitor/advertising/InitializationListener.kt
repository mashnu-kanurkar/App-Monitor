package com.redwater.appmonitor.advertising

import com.redwater.appmonitor.logger.Logger
import com.unity3d.mediation.LevelPlayConfiguration
import com.unity3d.mediation.LevelPlayInitError
import com.unity3d.mediation.LevelPlayInitListener

class InitializationListener(private val listener: ActivityListener) :
    LevelPlayInitListener {

    private val TAG = InitializationListener::class.java.name


    /**
    triggered when the initialization is completed successfully. After you receive this indication, ads can be loaded
    @param configuration The configuration
     */
    override fun onInitSuccess(configuration: LevelPlayConfiguration) {
        Logger.d(TAG, "LevelPlay initialized successfully")
        this.listener.createInterstitialAd()
        this.listener.createBannerAd()
    }

    /**
    the configuration was not retrieved successfully and ads cannot be loaded. It is recommended to try and initialize the levelPlay SDK later (when internet connection is available, or when the failure reason is resolved)
    @param error The reason for the error
     */
    override fun onInitFailed(error: LevelPlayInitError) {
        Logger.d(TAG, "LevelPlay initialization error = $error")
    }


}