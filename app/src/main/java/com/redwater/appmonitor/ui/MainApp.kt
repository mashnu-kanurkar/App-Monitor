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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.OnBoardingPreferences
import com.redwater.appmonitor.data.dataStore
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.ui.screens.AnalyticsScreen
import com.redwater.appmonitor.ui.screens.HomeScreen
import com.redwater.appmonitor.ui.screens.PermissionScreen
import com.redwater.appmonitor.viewmodel.AnalyticsViewModel
import com.redwater.appmonitor.viewmodel.MainViewModel
import com.redwater.appmonitor.viewmodel.PermissionViewModel
import com.redwater.appmonitor.viewmodel.ViewModelFactory

enum class AppScreens(@StringRes val title: Int, val route: String){
    HomeScreen(title = R.string.app_name, route = "homeScreen"),
    Analytics(title = R.string.analytics, route = "analytics/{packageName}"),
    Permission(title = R.string.permission, route = "permission")

}

fun getTitle(route: String?): Int {
    if (route?.contains("homeScreen") == true){
        return AppScreens.HomeScreen.title
    }
    if (route?.contains("analytics") == true){
        return AppScreens.Analytics.title
    }
    if (route?.contains("permission") == true){
        return AppScreens.Permission.title
    }
    return R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMonitorAppBar(
    title: Int,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(title)) },
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

@Composable
fun MainApp(repository: AppUsageStatsRepository,
            navController: NavHostController = rememberNavController(),
            onBoardingRequired: Int = 0,
            context: Context = LocalContext.current) {

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreenTitle = getTitle(backStackEntry?.destination?.route)
    Logger.d("MainApp", "onBoardingRequired: $onBoardingRequired")
    Scaffold(
        topBar = {
            AppMonitorAppBar(
                title = currentScreenTitle,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onBoardingRequired > 0) AppScreens.HomeScreen.route else AppScreens.Permission.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreens.Permission.route){
                val permissionViewModel = viewModel<PermissionViewModel>()
                PermissionScreen(permissionViewModel = permissionViewModel){
                    navController.navigate(route = AppScreens.HomeScreen.route){
                        popUpTo(navController.graph.id){
                            inclusive = true
                        }

                    }
                }
            }
            composable(route = AppScreens.HomeScreen.route){
                val mainViewModel = viewModel<MainViewModel>(factory = ViewModelFactory(repository) )
                HomeScreen(mainViewModel = mainViewModel,
                    modifier = Modifier.fillMaxSize(), context = context,
                    onNavigateNext = {packageName: String ->
                        Logger.d("Main App", "Navigating with $packageName")
                        navController.navigate(AppScreens.Analytics.route.replace(oldValue = "{packageName}", newValue = packageName))
                    },
                    onNavigateToPermissionScreen = {
                        navController.navigate(AppScreens.Permission.route)
                    }
                )
            }
            composable(route = AppScreens.Analytics.route, arguments = listOf(navArgument("packageName") { type = NavType.StringType })){ backStackEntry->
                val packageName = backStackEntry.arguments?.getString("packageName")
                Logger.d("Main App", "Navigating with $packageName")
                val analyticsViewModel = viewModel<AnalyticsViewModel>(factory = ViewModelFactory(repository) )
                AnalyticsScreen(analyticsViewModel = analyticsViewModel, packageName = packageName, modifier = Modifier.fillMaxSize(), context = context)
            }
        }
    }

}