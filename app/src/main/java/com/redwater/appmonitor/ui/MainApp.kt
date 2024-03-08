package com.redwater.appmonitor.ui

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.R
import com.redwater.appmonitor.advertising.ADManager
import com.redwater.appmonitor.advertising.BannerPlacement
import com.redwater.appmonitor.data.UserPreferences
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.BlogRepository
import com.redwater.appmonitor.data.repository.QuotesRepository
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.ui.screens.AnalyticsScreen
import com.redwater.appmonitor.ui.screens.DNDScreen
import com.redwater.appmonitor.ui.screens.Dashboard
import com.redwater.appmonitor.ui.screens.HomeScreen
import com.redwater.appmonitor.ui.screens.PermissionScreen
import com.redwater.appmonitor.ui.screens.PrivacyPolicyAgreementScreen
import com.redwater.appmonitor.viewmodel.AnalyticsViewModel
import com.redwater.appmonitor.viewmodel.DNDViewModel
import com.redwater.appmonitor.viewmodel.DNDViewModelFactory
import com.redwater.appmonitor.viewmodel.DashboardViewModel
import com.redwater.appmonitor.viewmodel.DashboardViewModelFactory
import com.redwater.appmonitor.viewmodel.MainViewModel
import com.redwater.appmonitor.viewmodel.PermissionViewModel
import com.redwater.appmonitor.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

enum class AppScreens(@StringRes val title: Int, val route: String){
    Dashboard(title = R.string.app_name, route = "dashboard"),
    HomeScreen(title = R.string.app_name, route = "homeScreen"),
    Analytics(title = R.string.analytics, route = "analytics/{packageName}"),
    Permission(title = R.string.permission, route = "permission"),
    PrivacyPolicy(title = R.string.privacy_policy_title, route = "privacy_policy"),
    DNDScreen(title = R.string.dnd_screen, route = "dnd_screen")

}

fun getTitle(route: String?): Int {
    if (route?.contains("dashboard") == true){
        return AppScreens.Dashboard.title
    }
    if (route?.contains("homeScreen") == true){
        return AppScreens.HomeScreen.title
    }
    if (route?.contains("analytics") == true){
        return AppScreens.Analytics.title
    }
    if (route?.contains("permission") == true){
        return AppScreens.Permission.title
    }
    if (route?.contains("privacy_policy") == true){
        return AppScreens.PrivacyPolicy.title
    }
    if (route?.contains("dnd_screen") == true){
        return AppScreens.DNDScreen.title
    }
    return R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMonitorAppBar(
    title: Int,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,

) {
    var expanded by remember {
        mutableStateOf(false)
    }
    val uriHandler = LocalUriHandler.current
    TopAppBar(
        title = { Text(text = stringResource(title), color = MaterialTheme.colorScheme.onPrimary) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { expanded = !expanded}) {
                Icon(imageVector = Icons.Filled.MoreVert, tint = MaterialTheme.colorScheme.onPrimary, contentDescription = "more")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = !expanded}
            ) {
                DropdownMenuItem(
                    text = { Text("Privacy policy") },
                    onClick = {
                        uriHandler.openUri(Constants.privacyPolicyURL)
                    }
                )
            }
        }
    )
}

@Composable
fun MainApp(appUsageStatsRepository: AppUsageStatsRepository,
            quotesRepository: QuotesRepository,
            blogRepository: BlogRepository,
            navController: NavHostController = rememberNavController(),
            onBoardingRequired: Int = 0,
            isPrivacyPolicyAccepted: Int = 0,
            context: Context = LocalContext.current) {

    val TAG = "MainApp"

    val coroutineScope = rememberCoroutineScope()
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreenTitle = getTitle(backStackEntry?.destination?.route)
    Logger.d(TAG, "onBoardingRequired: $onBoardingRequired")
    Logger.d(TAG, "privacy policy accepted: $isPrivacyPolicyAccepted")
    val startDestination = if (isPrivacyPolicyAccepted > 0) AppScreens.Dashboard.route else if (onBoardingRequired > 0) AppScreens.PrivacyPolicy.route else AppScreens.Permission.route
    //val startDestination = "test"
    Scaffold(
        topBar = {
            AppMonitorAppBar(
                title = currentScreenTitle,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
            )
        },
        bottomBar = {
            AndroidView(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(color = Color.Transparent),
                factory = {
                    IronSource.createBanner(context as Activity?, ISBannerSize.SMART)
                },
                update = {
                    Logger.d(TAG, "Banner layout inflated")
                    ADManager.getInstance(context.applicationContext).setBannerLayout(ironSourceBannerLayout = it, placement = BannerPlacement.HomeBanner())
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route= "test"){
               com.redwater.appmonitor.ui.screens.TestScreen()
            }
            composable(route = AppScreens.Dashboard.route){
                val dashboardViewModel = viewModel<DashboardViewModel>(factory = DashboardViewModelFactory(appUsageStatsRepository, quotesRepository, blogRepository))
                Dashboard(dashboardViewModel = dashboardViewModel, onNavigateToAppListing = {
                    navController.navigate(route = AppScreens.HomeScreen.route)
                }, onNavigateToDND = {
                    navController.navigate(route = AppScreens.DNDScreen.route)
                }
                )
            }
            composable(route = AppScreens.Permission.route){
                val permissionViewModel = viewModel<PermissionViewModel>()
                PermissionScreen(permissionViewModel = permissionViewModel){
                    navController.navigate(route = AppScreens.PrivacyPolicy.route){
                        popUpTo(navController.graph.id){
                            inclusive = true
                        }
                    }
                }
            }
            composable(route = AppScreens.PrivacyPolicy.route){
                PrivacyPolicyAgreementScreen {
                    coroutineScope.launch {
                        UserPreferences(context = context).updatePrivacyPolicyAcceptPrefs(isAccepted = 1)
                    }
                    navController.navigate(route = AppScreens.Dashboard.route){
                        popUpTo(navController.graph.id){
                            inclusive = true
                        }
                    }
                }
            }
            composable(route = AppScreens.HomeScreen.route){
                val mainViewModel = viewModel<MainViewModel>(factory = ViewModelFactory(appUsageStatsRepository) )
                HomeScreen(mainViewModel = mainViewModel,
                    modifier = Modifier.fillMaxSize(), context = context,
                    onNavigateNext = {packageName: String ->
                        Logger.d(TAG, "Navigating with $packageName")
                        navController.navigate(AppScreens.Analytics.route.replace(oldValue = "{packageName}", newValue = packageName))
                    },
                    onNavigateToPermissionScreen = {
                        navController.navigate(AppScreens.Permission.route)
                    }
                )
            }
            composable(route = AppScreens.Analytics.route, arguments = listOf(navArgument("packageName") { type = NavType.StringType })){ backStackEntry->
                val packageName = backStackEntry.arguments?.getString("packageName")
                Logger.d(TAG, "Navigating with $packageName")
                val analyticsViewModel = viewModel<AnalyticsViewModel>(factory = ViewModelFactory(appUsageStatsRepository) )
                AnalyticsScreen(analyticsViewModel = analyticsViewModel,
                    packageName = packageName, modifier = Modifier.fillMaxSize(),
                    context = context
                    )
            }

            composable(route = AppScreens.DNDScreen.route){
                Logger.d(TAG, "Navigating to DND")
                val dndViewModel = viewModel<DNDViewModel>(factory = DNDViewModelFactory(appUsageStatsRepository))
                DNDScreen(dndViewModel = dndViewModel)
            }

        }
    }
}