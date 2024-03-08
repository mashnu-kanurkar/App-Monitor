package com.redwater.appmonitor.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ironsource.mediationsdk.IronSource
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.redwater.appmonitor.R
import com.redwater.appmonitor.logger.Logger
import com.redwater.appmonitor.ui.components.CombinedStatsCard
import com.redwater.appmonitor.ui.components.LoadingIndicator
import com.redwater.appmonitor.ui.components.PackageInfoCard
import com.redwater.appmonitor.ui.components.PermissionAlertDialog
import com.redwater.appmonitor.ui.components.SessionDataCard
import com.redwater.appmonitor.ui.components.TimeSelectionDialog
import com.redwater.appmonitor.ui.components.UsageIndicator
import com.redwater.appmonitor.utils.TimeFormatUtility
import com.redwater.appmonitor.utils.VicoChartUtility
import com.redwater.appmonitor.viewmodel.AnalyticsViewModel
import com.patrykandpatrick.vico.compose.axis.vertical.*
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import com.redwater.appmonitor.data.model.hourlyDistributionInMillis
import com.redwater.appmonitor.data.model.maxOrNull

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    analyticsViewModel: AnalyticsViewModel,
    packageName: String?,
    context: Context = LocalContext.current
) {

    val TAG = "AnalyticsScreen"
    val uiState by analyticsViewModel.analyticsState
    val monthlyStats = analyticsViewModel.monthlyStats
    val permissionStateMap = analyticsViewModel.permissionStateMap
    val vicoUtility = remember {
        VicoChartUtility()
    }

    DisposableEffect(key1 = LocalLifecycleOwner.current,){
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                Logger.d(TAG, "on create")
                analyticsViewModel.getPermissionState(context = context)
                packageName?.let {
                    analyticsViewModel.getPackageInfo(packageName = packageName, context = context)
                    analyticsViewModel.getMonthlyStats(packageName = packageName, context = context)
                }
            }
        }
        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)
        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Box(modifier = modifier.fillMaxSize()) {

        if (packageName == null) {
            ElevatedCard(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.invalid_app),
                    color = MaterialTheme.colorScheme.error
                )
            }

        }else{
            Column(modifier = modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Top) {
                if (uiState.appModel != null) {
                    val usageTimeInMin = (uiState.appModel!!.usageTimeInMillis / (60 * 1000)).toShort()
                    val sessionEntries = vicoUtility.getEntriesWithLabel(uiState.appModel!!.session?.hourlyDistributionInMillis()?: mutableMapOf())
                    // val entries = vicoUtility.getEntriesWithLabel(uiState.appModel!!.usageDistribution)
                    val monthlyStatsEntries = vicoUtility.getMonthlyEntriesWithLabel(monthlyStatsList = monthlyStats)
                    val chartScrollSpec = rememberChartScrollSpec<ChartEntryModel>(initialScroll = InitialScroll.End)
                    PackageInfoCard(
                        modifier = Modifier.padding(4.dp, 8.dp),
                        icon = uiState.appModel!!.icon?:context.packageManager.getApplicationIcon(context.packageName).toBitmap(48, 48).asImageBitmap(),
                        name = uiState.appModel!!.name,
                        packageName = uiState.appModel!!.packageName,
                        isSelected = uiState.appModel!!.isSelected,
                        usageTimeInMin = usageTimeInMin,
                        usageIndicator = {
                            UsageIndicator(
                                usageTimeInMin = usageTimeInMin.toFloat(),
                                thresholdTimeInMin = uiState.appModel!!.thresholdTime?.toFloat()
                            )
                        },
                        onClick = {},
                        onChangePref = {
                            analyticsViewModel.onAppPrefsClickEvent(
                                packageName = packageName,
                                isSelected = !uiState.appModel!!.isSelected,
                            )
                        }
                    )
                    CombinedStatsCard(modifier = Modifier.padding(4.dp, 8.dp), todayUsageInMin = usageTimeInMin, launchCount = uiState.appModel!!.session?.sessionList?.size?: 0, longestSessionTimeInSec = (uiState.appModel!!.session?.maxOrNull()?.sessionLength?:0)/1000, thresholdTimeInMin = uiState.appModel!!.thresholdTime)

                    Divider(modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth())
                    if (sessionEntries.first.isEmpty().not()){
                        Card(modifier = Modifier.padding(4.dp, 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp)) {
                            Text(modifier = Modifier
                                .padding(8.dp, 8.dp), text = stringResource(id = R.string.today_usage_trend), style = MaterialTheme.typography.titleLarge)
                            Chart(modifier = Modifier.padding(8.dp),
                                chart = columnChart(),
                                chartModelProducer = ChartEntryModelProducer(sessionEntries.first),
                                startAxis = startAxis(),
                                bottomAxis = bottomAxis(valueFormatter = { value, _ ->
                                    sessionEntries.second[value.toInt()]
                                }),
                                chartScrollSpec = chartScrollSpec,

                                )
                        }
                    }
                    Divider(modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth())
                    if (monthlyStatsEntries.first.isEmpty().not()){
                        Card(modifier = Modifier.padding(4.dp, 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp)) {
                            Text(modifier = Modifier
                                .padding(8.dp, 8.dp), text = stringResource(id = R.string.history_usage_trend), style = MaterialTheme.typography.titleLarge)
                            Chart(modifier = Modifier.padding(8.dp),
                                chart = lineChart(),
                                chartModelProducer = ChartEntryModelProducer(monthlyStatsEntries.first),
                                startAxis = startAxis(),
                                bottomAxis = bottomAxis(valueFormatter = { value, _ ->
                                    monthlyStatsEntries.second[value.toInt()]
                                }),
                                chartScrollSpec = chartScrollSpec,

                                )
                        }
                    }
                    Divider(modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth())
                    SessionDataCard(sessionData = uiState.appModel!!.session, format = "hh:mm:ss aa")

                } else {
                    if (uiState.dataLoadingState.show.not()){
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.default_error_desc),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            if (uiState.dataLoadingState.show) {
                LoadingIndicator(message = uiState.dataLoadingState.message)
            }
            if (uiState.showTimePopUp) {
                val descriptionWithAppName =
                    stringResource(id = R.string.threshold_time_description).replace(
                        oldValue = "##app_name##",
                        newValue = uiState.appModel?.name!!
                    )
                TimeSelectionDialog(title = stringResource(id = R.string.threshold_time_title),
                    description = descriptionWithAppName,
                    //timeList = timeList,
                    onSelection = {
                        Logger.d(TAG, "Selected time $it")
                        analyticsViewModel.onTimeSelection(
                            isDismiss = false,
                            durationModel = it,
                            context = context.applicationContext,
                            packageName = packageName
                        )
                        if (IronSource.isInterstitialReady()) IronSource.showInterstitial()
                    }) {
                    analyticsViewModel.onTimeSelection(isDismiss = true)
                }
                if (uiState.permissionPopUpState.show) {
                    PermissionAlertDialog(
                        title = stringResource(id = R.string.permission_required_title),
                        message = permissionStateMap.get(uiState.permissionPopUpState.permissionType)?.errorDescription
                            ?: "",
                        onClickNo = {
                            analyticsViewModel.onPopUpClick(
                                type = uiState.permissionPopUpState.permissionType,
                                isPositive = false,
                                context = context
                            )
                        },
                        onClickYes = {
                            analyticsViewModel.onPopUpClick(
                                type = uiState.permissionPopUpState.permissionType,
                                isPositive = true,
                                context = context
                            )
                        }) {
                        analyticsViewModel.onPopUpClick(
                            type = uiState.permissionPopUpState.permissionType,
                            isPositive = false,
                            context = context
                        )
                    }
                }
            }
        }
    }
}