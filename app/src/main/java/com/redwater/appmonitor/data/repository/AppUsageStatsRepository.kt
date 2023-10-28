package com.redwater.appmonitor.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.AppPrefsDao
import com.redwater.appmonitor.data.model.AppAndTime
import com.redwater.appmonitor.data.model.toAppModel
import com.redwater.appmonitor.data.model.toAppRoomModel
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.Exception

class AppUsageStatsRepository(private val appPrefsDao: AppPrefsDao) {

    private val TAG = "PreferenceRepository"

    suspend fun getAllRecords(): List<AppModel>{
        Logger.d("$TAG => fetching all records")
        return withContext(Dispatchers.IO){
            val appRoomModelList = appPrefsDao.getAllRecords()
            val appModelList = mutableListOf<AppModel>()
            appRoomModelList.forEach {
                appModelList.add(it.toAppModel())
            }
            return@withContext appModelList
        }
    }

    suspend fun insertPrefsFor(appModel: AppModel){
        Logger.d("$TAG => inserting record $appModel")
        withContext(Dispatchers.IO){
            appPrefsDao.insert(appModel.toAppRoomModel())
        }
    }

    suspend fun deletePrefsFor(packageName: String){
        Logger.d("$TAG => deleting record for $packageName")
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
                    AppModel(packageName = packageName, name = appName, icon = iconDrawable)
            }
            return@withContext allAppMap
        }
    }
    suspend fun getAppUsageStatsFor(packageName: String, context: Context):AppAndTime?{
        val usageStatsMap = getAppUsageStats(context, hashMapOf(packageName to AppModel(packageName = packageName)))
        return if (usageStatsMap.isEmpty()){
            null
        }else{
            val packageName = usageStatsMap.keys.first()
            var timeInMin: Short = 0
            usageStatsMap[packageName]?.let {
                timeInMin = (it.usageTime/(1000*60)).toShort()
            }
            AppAndTime(
                packageName = packageName,
                time = timeInMin
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
                        val applicationInfo = getApplicationInfo(context = context, packageName = event.packageName, packageManager = packageManager)
                        if (isSystemApp(applicationInfo = applicationInfo).not() && (event.packageName == (context.packageName)).not()){
//                            allUsageMap[event.packageName] =
//                                AppUsageStats(packageName = event.packageName,
//                                    appName = getApplicationLabel(applicationInfo = applicationInfo, packageManager = packageManager),
//                                    appIcon = context.packageManager.getApplicationIcon(event.packageName))
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

//                //for launchCount of apps in time range
//                if (!e0.packageName.equals(e1.packageName) && e1.eventType ==1){
//                    // if true, E1 (launch event of an app) app launched
//                    allUsageMap.get(e1.packageName)?.let {
//                        ++ it.launchCount
//                    }
//                }
                //for UsageTime of apps in time range
                if (e0.eventType == 1 && e1.eventType == 2
                    && e0.className.equals(e1.className)){
                    val diff = e1.timeStamp - e0.timeStamp;
                    allUsageMap.get(e0.packageName)?.let {
                        it.usageTime += diff
                    }
                    //We can not use direct short conversion here, as the error rate directly proportional to event count (launch count)
//                    if (e0.packageName.contains("com.instagram.android")){
//                        instaUsageInShort = (instaUsageInShort + (diff/(1000*60))).toShort()
//                    }
                }

            }
            return@withContext allUsageMap
        }
    }

    private fun getApplicationInfo(context: Context, packageName: String, packageManager: PackageManager): ApplicationInfo?{
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

}