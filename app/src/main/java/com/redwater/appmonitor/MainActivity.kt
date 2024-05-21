package com.redwater.appmonitor

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.redwater.appmonitor.advertising.ADManager
import com.redwater.appmonitor.data.UserPreferences
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.BlogRepository
import com.redwater.appmonitor.data.repository.QuotesRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.service.RemoteMessagingService
import com.redwater.appmonitor.service.ServiceManager
import com.redwater.appmonitor.ui.MainApp
import com.redwater.appmonitor.ui.theme.AppMonitorTheme
import com.redwater.appmonitor.workmanager.FirebaseSyncWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity(){
    private val TAG = this::class.simpleName
    private lateinit var appUsageStatsRepository: AppUsageStatsRepository
    private lateinit var quotesRepository: QuotesRepository
    private lateinit var blogRepository: BlogRepository
    private var isOnBoardingCompleted: Int = -1
    private lateinit var onboardingPrefsCollector: Job
    private var isPrivacyPolicyAccepted: Int = -1
    private lateinit var privacyPolicyAcceptFlowCollector: Job
    private lateinit var appPrefsMonitorCollector: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")
        val token = RemoteMessagingService().getToken(applicationContext)
        Logger.d(TAG, "token $token")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            splashScreen.setOnExitAnimationListener {splashScreenView ->
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 200L

                // Call SplashScreenView.remove at the end of your custom animation.
                slideUp.doOnEnd { splashScreenView.remove() }

                // Run your animation.
                slideUp.start()
            }
        }
        appUsageStatsRepository = (application as AppMonitorApp).appPrefsRepository
        quotesRepository = (application as AppMonitorApp).quotesRepository
        blogRepository = (application as AppMonitorApp).blogRepository


        onboardingPrefsCollector = lifecycleScope.launch {
            UserPreferences(context = this@MainActivity).onboardingCompletedFlow.collectLatest {
                Logger.d(TAG, "fetched onboarding prefs $it")
                isOnBoardingCompleted = it
            }
        }

        privacyPolicyAcceptFlowCollector = lifecycleScope.launch {
            UserPreferences(context = this@MainActivity).privacyPolicyAcceptFlow.collectLatest {
                Logger.d(TAG, "fetched privacy policy prefs $it")
                isPrivacyPolicyAccepted = it
            }
        }

        IntegrationHelper.validateIntegration(this)

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener{
                override fun onPreDraw(): Boolean {
                    return if (isOnBoardingCompleted >= 0 && isPrivacyPolicyAccepted >= 0){
                        Logger.d(TAG, "T -> isOnBoardingCompleted: $isOnBoardingCompleted")
                        Logger.d(TAG, "F -> isPrivacyPolicyAccepted: $isPrivacyPolicyAccepted")
                        onboardingPrefsCollector.cancel()
                        privacyPolicyAcceptFlowCollector.cancel()
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        val adManager = ADManager.getInstance(applicationContext)
                        adManager.initialiseIronSource()
                        lifecycle.addObserver(adManager)
                        setContent {
                            AppMonitorTheme {
                                Logger.d(TAG, "update $isOnBoardingCompleted")
                                    MainApp(appUsageStatsRepository = appUsageStatsRepository,
                                        quotesRepository = quotesRepository,
                                        blogRepository = blogRepository,
                                        onBoardingRequired = isOnBoardingCompleted,
                                        isPrivacyPolicyAccepted = isPrivacyPolicyAccepted,
                                        context = this@MainActivity
                                    )
                            }
                        }
                        true
                    }else{
                        Logger.d(TAG, "F -> isOnBoardingCompleted: $isOnBoardingCompleted")
                        Logger.d(TAG, "F -> isPrivacyPolicyAccepted: $isPrivacyPolicyAccepted")
                        false
                    }
                }
            }
        )
        monitorService()
        val periodWork = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(Constants.firebaseSyncWorkerPeriodInDays,
            TimeUnit.DAYS
        ).build()
        WorkManager.getInstance(this.applicationContext)
            .enqueueUniquePeriodicWork(Constants.firebaseSyncPeriodicWorkerTag, ExistingPeriodicWorkPolicy.KEEP, periodWork)

    }

    private fun monitorService(){
        appPrefsMonitorCollector = lifecycleScope.launch {
            appUsageStatsRepository.getAllSelectedRecordsFlow().collectLatest {
                ServiceManager.toggleService(context = applicationContext, repository = appUsageStatsRepository)
            }
        }
    }

    override fun onDestroy() {
        onboardingPrefsCollector.cancel()
        privacyPolicyAcceptFlowCollector.cancel()
        appPrefsMonitorCollector.cancel()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }
}