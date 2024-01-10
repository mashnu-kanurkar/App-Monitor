package com.redwater.appmonitor

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ComposeView
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.redwater.appmonitor.data.OnBoardingPreferences
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.service.ServiceManager
import com.redwater.appmonitor.ui.MainApp
import com.redwater.appmonitor.ui.theme.AppMonitorTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class MainActivity : ComponentActivity() {

    private val TAG = this::class.simpleName
    private lateinit var repository: AppUsageStatsRepository
    private var isOnBoardingCompleted: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

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
        repository = (application as AppMonitorApp).appPrefsRepository

        lifecycleScope.launch {
            OnBoardingPreferences(context = applicationContext).onboardingCompletedFlow.collectLatest {
                Logger.d(TAG, "fetched onboarding prefs $it")
                isOnBoardingCompleted = it
            }
        }

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener{
                override fun onPreDraw(): Boolean {
                    return if (isOnBoardingCompleted >= 0){
                        Logger.d(TAG, "T -> isOnBoardingCompleted: $isOnBoardingCompleted")
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        setContent {
                            AppMonitorTheme {
                                Logger.d(TAG, "update $isOnBoardingCompleted")
                                MainApp(repository = repository, onBoardingRequired = isOnBoardingCompleted)
                            }
                        }
                        true
                    }else{
                        Logger.d(TAG, "F -> isOnBoardingCompleted: $isOnBoardingCompleted")
                        false
                    }
                }
            }
        )


        monitorService()
    }

    private fun monitorService(){
        lifecycleScope.launch {
            repository.getAllSelectedRecordsFlow().collectLatest {
                ServiceManager.toggleService(context = applicationContext, repository = repository)
            }
        }
    }
}