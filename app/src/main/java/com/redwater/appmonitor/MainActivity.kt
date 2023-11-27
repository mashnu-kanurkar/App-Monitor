package com.redwater.appmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.redwater.appmonitor.ui.MainApp
import com.redwater.appmonitor.ui.theme.AppMonitorTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as AppMonitorApp).appPrefsRepository

        setContent {
            AppMonitorTheme {
                MainApp(repository = repository)
            }
        }


    }
}