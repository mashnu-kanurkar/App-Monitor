package com.redwater.appmonitor

import android.R
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.redwater.appmonitor.ui.screens.HomeScreen
import com.redwater.appmonitor.ui.theme.AppMonitorTheme
import com.redwater.appmonitor.viewmodel.MainViewModel
import com.redwater.appmonitor.viewmodel.MainViewModelFactory


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as AppMonitorApp).appPrefsRepository
        val viewModel = ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)

        setContent {
            AppMonitorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(mainViewModel = viewModel)
                }
            }
        }


    }
}