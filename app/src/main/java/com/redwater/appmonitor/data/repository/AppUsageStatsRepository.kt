package com.redwater.appmonitor.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.redwater.appmonitor.data.AppPrefsDao
import com.redwater.appmonitor.data.model.AppDataFromSystem
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.toAppModel
import com.redwater.appmonitor.data.model.toAppRoomModel
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppUsageStatsRepository(private val appPrefsDao: AppPrefsDao) {

    private val TAG = this::class.simpleName

    suspend fun getAllRecords(): List<AppModel>{
        Logger.d(TAG, "fetching all records")
        return withContext(Dispatchers.IO){
            val appRoomModelList = appPrefsDao.getAllRecords()
            val appModelList = mutableListOf<AppModel>()
            appRoomModelList.forEach {
                appModelList.add(it.toAppModel())
            }
            return@withContext appModelList
        }
    }

    suspend fun addDelay(packageName: String,delayInMin: Short){
        Logger.d(TAG, "adding delay of $delayInMin for $packageName")
        withContext(Dispatchers.IO){
            appPrefsDao.updateDelay(packageName, delayInMin)
        }
    }

    suspend fun insertPrefsFor(appModel: AppModel){
        Logger.d(TAG, "inserting record $appModel")
        withContext(Dispatchers.IO){
            appPrefsDao.insert(appModel.toAppRoomModel())
        }
    }

    suspend fun deletePrefsFor(packageName: String){
        Logger.d(TAG, "deleting record for $packageName")
        withContext(Dispatchers.IO){
            appPrefsDao.deleteAppPrefs(packageName)
        }
    }

    suspend fun getAllAvailableApps(context: Context):HashMap<String, AppModel> {
        return withContext(Dispatchers.Default){
            val allAppMap = hashMapOf<String, AppModel>()
            // one of the resolved info from the package manager
            val pm = context.packageManager
            // searching main activities labeled to be launchers of the apps
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(
                    mainIntent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                pm.queryIntentActivities(mainIntent, 0)
            }
            resolvedInfos.forEach {resolveInfo->
                val resources =  pm.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo)
                val appName = if (resolveInfo.activityInfo.labelRes != 0) {
                    // getting proper label from resources
                    resources.getString(resolveInfo.activityInfo.labelRes)
                } else {
                    // getting it out of app info - equivalent to context.packageManager.getApplicationInfo
                    resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString()
                }
                val packageName = resolveInfo.activityInfo.packageName
                val iconDrawable = resolveInfo.activityInfo.loadIcon(pm)
                allAppMap[packageName] =
                    AppModel(packageName = packageName, name = appName, icon = iconDrawable.toBitmap(48, 48).asImageBitmap())
            }
            return@withContext allAppMap
        }
    }
    suspend fun getAppUsageStatsFor(packageName: String, context: Context):AppDataFromSystem?{
        val usageStatsMap = getAppUsageStats(context, hashMapOf(packageName to AppModel(packageName = packageName)))
        return if (usageStatsMap.isEmpty()){
            null
        }else{
            val packageNameFirstKey = usageStatsMap.keys.first()
            var timeInMin: Short = 0
            var usageDist: MutableMap<Short, Long> = mutableMapOf()
            usageStatsMap[packageName]?.let {
                timeInMin = (it.usageTime/(1000*60)).toShort()
                usageDist = it.usageDistribution
            }
            usageDist.map {
                Logger.d(TAG, "usageDist: key ${it.key}: value ${it.value}")
            }
            AppDataFromSystem(
                packageName = packageNameFirstKey,
                usageTime = timeInMin,
                usageDist = usageDist
            )
        }
    }
    suspend fun getAllAppUsageStats(context: Context, allAppMap: HashMap<String, AppModel>):HashMap<String, AppModel>{
        return getAppUsageStats(context = context, allUsageMap = allAppMap)
    }
    private suspend fun getAppUsageStats(context: Context, allUsageMap: HashMap<String, AppModel>): HashMap<String, AppModel>{
        return withContext(Dispatchers.Default){
            val allEventList = mutableListOf<UsageEvents.Event>()
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.set(Calendar.AM_PM, Calendar.AM)
            val startTime: Long = calendar.timeInMillis
            val endTime = System.currentTimeMillis()
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)

            while (usageEvents.hasNextEvent()) {
                val event = UsageEvents.Event()
                usageEvents.getNextEvent(event)
                try {
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        if (allUsageMap.containsKey(event.packageName).not()){
                            continue
                        }
                        val packageManager = context.packageManager
                        val applicationInfo = getApplicationInfo(
                            packageName = event.packageName,
                            packageManager = packageManager
                        )
                        //do not add if it is system app or self app (App monitor)
                        if (isSystemApp(applicationInfo = applicationInfo).not() && (event.packageName == (context.packageName)).not()){
                           allEventList.add(event)
                        }
                    }
                }catch (packageNotFoundException: PackageManager.NameNotFoundException){
                    allUsageMap.remove(event.packageName)
                    continue
                }
                catch (e:Exception){
                    e.printStackTrace()
                    continue
                }
            }
            for (index in 0 until (allEventList.size-1)){
                val e0 = allEventList[index]
                val e1 = allEventList[(index + 1)]
                //for launchCount of apps in time range
                if (!e0.packageName.equals(e1.packageName) && e1.eventType ==1){
                    // if true, E1 (launch event of an app) app launched
                    allUsageMap.get(e1.packageName)?.let {
                        ++ it.launchCountToday
                    }
                }
                //for UsageTime of apps in time range
                //1 = resumed and 2 = paused
                if (e0.eventType == 1 && e1.eventType == 2
                    && e0.className.equals(e1.className)){
                    val diff = e1.timeStamp - e0.timeStamp;
                    Logger.d(TAG, "package wise usage dist,${e0.packageName},${e0.eventType},${e0.timeStamp},${e1.packageName},${e1.eventType},${e1.timeStamp}")
                    allUsageMap[e0.packageName]?.let {
                        it.usageTime += diff
                        val hr0: Short = ((e0.timeStamp - calendar.timeInMillis)/(60*1000*60)).toShort()
                        val hr1: Short = ((e1.timeStamp - calendar.timeInMillis)/(60*1000*60)).toShort()
                        if (hr0 == hr1){
                            val oldValue: Long = it.usageDistribution[hr0] ?:0
                            it.usageDistribution.put(key = hr0, value = oldValue + (diff/1000))
                            Logger.d(TAG, "Updated distribution (hr0==hr1) hr0: key $hr0 value ${oldValue + (diff/(60*1000))}")
                        }else{
                            val oldValue0: Long = it.usageDistribution[hr0] ?:0
                            val oldValue1: Long = it.usageDistribution[hr1] ?:0
                            val postHourSecondsInMilli = (e0.timeStamp - calendar.timeInMillis)%(60*1000*60)
                            val postHourSeconds = postHourSecondsInMilli/1000
                            val secondsTillNextHour = (3600 - postHourSeconds)
                            it.usageDistribution.put(key = hr0, value = oldValue0 + secondsTillNextHour)
                            it.usageDistribution.put(key = hr1, value = oldValue1 + ((diff/1000) - secondsTillNextHour))
                            Logger.d(TAG, "Updated distribution (hr0 /= hr1) hr0: key $hr0 value ${oldValue0 + secondsTillNextHour}")
                            Logger.d(TAG, "Updated distribution (hr0 /= hr1) hr1: key $hr1 value ${oldValue1 + diff - secondsTillNextHour}")
                        }
                    }
                }

            }
            return@withContext allUsageMap
        }
    }

    private fun getApplicationInfo(packageName: String, packageManager: PackageManager): ApplicationInfo?{
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            }else{
                packageManager.getApplicationInfo(packageName, 0)
            }

        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun isSystemApp(applicationInfo: ApplicationInfo?): Boolean{
        applicationInfo?.let {
            if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0){
                return true
            }
        }
        return false
    }

    private fun getApplicationLabel(applicationInfo: ApplicationInfo?, packageManager: PackageManager): String{
        return  applicationInfo?.let { appInfo ->
            packageManager.getApplicationLabel(appInfo).toString()
        } ?: "No Name"
    }

    fun getAppsUsageData(context: Context){
        val pm = context.packageManager
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

}