package com.redwater.appmonitor.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.ui.screens.AnalyticsScreen
import com.redwater.appmonitor.ui.screens.HomeScreen
import com.redwater.appmonitor.viewmodel.AnalyticsViewModel
import com.redwater.appmonitor.viewmodel.MainViewModel
import com.redwater.appmonitor.viewmodel.ViewModelFactory

enum class AppScreens(@StringRes val title: Int, val route: String){
    HomeScreen(title = R.string.app_name, route = "homeScreen"){
        override fun getRoute(vararg arg: String): String {
            return route
        }

        override fun getScreen(route: String): AppScreens {
            return this
        }
    },
    Analytics(title = R.string.analytics, route = "analytics/{packageName}"){
        fun createRoute(packageName: String) = "analytics/$packageName"
        override fun getRoute(vararg arg: String): String {
            return "analytics/$arg"
        }

        override fun getScreen(route: String): AppScreens {
            return this
        }
    };
    abstract fun getRoute(vararg arg: String): String

    abstract fun getScreen(route: String): AppScreens
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMonitorAppBar(
    currentScreen: AppScreens,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(repository: AppUsageStatsRepository,
            navController: NavHostController = rememberNavController(),
            context: Context = LocalContext.current) {

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = AppScreens().getScreen()
    Scaffold(
        topBar = {
            AppMonitorAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.HomeScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreens.HomeScreen.route){
                val mainViewModel = viewModel<MainViewModel>(factory = ViewModelFactory(repository) )
                HomeScreen(mainViewModel = mainViewModel, modifier = Modifier.fillMaxSize(), context = context){packageName: String ->
                    navController.navigate(AppScreens.Analytics.getRoute(packageName))
                }
            }
            composable(route = AppScreens.Analytics.route){backStackEntry->
                val packageName = backStackEntry.arguments?.getString("packageName")
                val analyticsViewModel = viewModel<AnalyticsViewModel>(factory = ViewModelFactory(repository) )
                AnalyticsScreen(analyticsViewModel = analyticsViewModel, packageName = packageName, modifier = Modifier.fillMaxSize(), context = context)
            }
        }
    }

}