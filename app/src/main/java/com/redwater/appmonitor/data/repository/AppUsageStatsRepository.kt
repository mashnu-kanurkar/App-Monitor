package com.redwater.appmonitor.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserManager
import androidx.compose.foundation.lazy.layout.IntervalList
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.redwater.appmonitor.data.AppPrefsDao
import com.redwater.appmonitor.data.model.AppDataFromSystem
import com.redwater.appmonitor.data.model.AppModel
import com.redwater.appmonitor.data.model.AppRoomModel
import com.redwater.appmonitor.data.model.Session
import com.redwater.appmonitor.data.model.hourlyDistributionInMillis
import com.redwater.appmonitor.data.model.toAppModel
import com.redwater.appmonitor.data.model.toAppRoomModel
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppUsageStatsRepository(private val appPrefsDao: AppPrefsDao) {

    private val TAG = this::class.simpleName

    suspend fun getAllSelectedRecords(): List<AppModel>{
        Logger.d(TAG, "fetching all selected records")
        return withContext(Dispatchers.IO){
            val appRoomModelList = appPrefsDao.getAllSelectedRecords()
            val appModelList = mutableListOf<AppModel>()
            appRoomModelList.forEach {
                appModelList.add(it.toAppModel())
            }
            return@withContext appModelList
        }
    }
    suspend fun getAllSelectedRecordsFlow(): Flow<List<AppRoomModel>> {
        Logger.d(TAG, "fetching all selected records")
        return withContext(Dispatchers.IO){
            return@withContext appPrefsDao.getAllSelectedRecordsFlow()
        }
    }

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
    suspend fun getAllRecordsFlow(): Flow<List<AppRoomModel>> {
        Logger.d(TAG, "fetching all records")
        return withContext(Dispatchers.IO){
            return@withContext appPrefsDao.getAllRecordsFlow()
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

    suspend fun unselectPrefsFor(packageName: String){
        Logger.d(TAG, "Unselecting record for $packageName")
        withContext(Dispatchers.IO){
            appPrefsDao.unselectPrefsFor(packageName)
        }
    }

    suspend fun getSavedPrefsFor(packageName: String): Flow<AppRoomModel?> {
        Logger.d(TAG, "fetching record for $packageName")
        return withContext(Dispatchers.IO){
            return@withContext appPrefsDao.getAppPrefsFor(packageName)
        }
    }
    suspend fun getAppModelData(packageName: String? = null, context: Context, enableSessionData:Boolean = false): HashMap<String, AppModel> {
        val appMetadata = getAppMetadata(context = context, packageName = packageName)
        Logger.d(TAG, "queried app metadata: $appMetadata")
        return getAppUsageStats(context = context, appUsageMap = appMetadata, enableSessionData = enableSessionData)
    }
    private suspend fun getAppMetadata(context: Context, packageName: String?):HashMap<String, AppModel> {
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
            resolvedInfos.dropWhile { if(packageName != null) it.activityInfo.packageName != packageName else false }.forEach {resolveInfo->
                val currentPackageName = resolveInfo.activityInfo.packageName
                val resources =  pm.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo)
                val appName = if (resolveInfo.activityInfo.labelRes != 0) {
                    // getting proper label from resources
                    resources.getString(resolveInfo.activityInfo.labelRes)
                } else {
                    // getting it out of app info - equivalent to context.packageManager.getApplicationInfo
                    resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString()
                }

                val iconDrawable = resolveInfo.activityInfo.loadIcon(pm)
                allAppMap[currentPackageName] =
                    AppModel(packageName = currentPackageName, name = appName, icon = iconDrawable.toBitmap(48, 48).asImageBitmap())
                if (packageName != null && currentPackageName == packageName){
                    return@withContext allAppMap
                }
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
            var usageDist: Map<Short, Long>? = mutableMapOf()
            usageStatsMap[packageName]?.let {
                timeInMin = (it.usageTimeInMillis/(1000*60)).toShort()
                usageDist = it.session?.hourlyDistributionInMillis()
            }

            AppDataFromSystem(
                packageName = packageNameFirstKey,
                usageTime = timeInMin,
                usageDist = usageDist
            )
        }
    }
    private suspend fun  getAppUsageStats(context: Context, appUsageMap: HashMap<String, AppModel>, enableSessionData: Boolean = false): HashMap<String, AppModel>{
        Logger.d(TAG, "getting stats for $appUsageMap")
        val userManager = context.getSystemService( Context.USER_SERVICE ) as UserManager
        if (userManager.isUserUnlocked.not()){
            return appUsageMap
        }
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
                        if (appUsageMap.containsKey(event.packageName).not()){
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
                    appUsageMap.remove(event.packageName)
                    continue
                }
                catch (e:Exception){
                    e.printStackTrace()
                    continue
                }
            }
            for (index in 0 until (allEventList.size-1)){
                //1 = resumed and 2 = paused
                val e0 = allEventList[index]
                val e1 = allEventList[(index + 1)]
                Logger.d(TAG, "QA-sessions,${e0.packageName},${e0.className},${e0.eventType},${e0.timeStamp},${e1.packageName},${e1.className},${e1.eventType},${e1.timeStamp}")
                //for launchCount of apps in time range, count the number of sessions
                //for UsageTime of apps in time range

                if (e0.eventType == 1 && e1.eventType == 2
                    && e0.className.equals(e1.className)){
                    val diff = e1.timeStamp - e0.timeStamp;
                    //Logger.d(TAG, "package wise usage dist,${e0.packageName},${e0.eventType},${e0.timeStamp},${e1.packageName},${e1.eventType},${e1.timeStamp}")
                    appUsageMap[e0.packageName]?.let {
                            val oldSession = it.session
                            if (oldSession == null){
                                it.session = Session(start = e0.timeStamp, end = e1.timeStamp)
                            }else{
                                it.session?.addSession(start = e0.timeStamp, e1.timeStamp)
                            }
//                            val sessionKey = (e0.timeStamp/100000)*100000
//                            val oldSessionLength: Long = it.sessions[sessionKey]?:0
//                            val newSessionLength = oldSessionLength + diff
//                            it.sessions.put(key = sessionKey, value = newSessionLength)

                        it.usageTimeInMillis += diff
// the existing method to calculate the hourly distribution
//                        val hr0: Short = ((e0.timeStamp - calendar.timeInMillis)/(60*1000*60)).toShort()
//                        val hr1: Short = ((e1.timeStamp - calendar.timeInMillis)/(60*1000*60)).toShort()
//                        if (hr0 == hr1){
//                            val oldValue: Long = it.usageDistribution[hr0] ?:0
//                            it.usageDistribution.put(key = hr0, value = oldValue + (diff/1000))
//                            //Logger.d(TAG, "Updated distribution (hr0==hr1) hr0: key $hr0 value ${oldValue + (diff/(60*1000))}")
//                        }else{
//                            val oldValue0: Long = it.usageDistribution[hr0] ?:0
//                            val oldValue1: Long = it.usageDistribution[hr1] ?:0
//                            val postHourSecondsInMilli = (e0.timeStamp - calendar.timeInMillis)%(60*1000*60)
//                            val postHourSeconds = postHourSecondsInMilli/1000
//                            val secondsTillNextHour = (3600 - postHourSeconds)
//                            it.usageDistribution.put(key = hr0, value = oldValue0 + secondsTillNextHour)
//                            it.usageDistribution.put(key = hr1, value = oldValue1 + ((diff/1000) - secondsTillNextHour))
//                        }
                    }
                }

            }
            return@withContext appUsageMap
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

    fun getUsageStatisticV2(context: Context){
        Logger.d(TAG, "getting stats V2")
        val userManager = context.getSystemService( Context.USER_SERVICE ) as UserManager
        if (userManager.isUserUnlocked.not()){
            return //appUsageMap
        }
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
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        usageStats.forEach {
            Logger.d(TAG, "Usage statistics V2,${it.packageName},${it.totalTimeInForeground},${it.firstTimeStamp},${it.lastTimeStamp}")
        }
    }

    suspend fun getAllUsageEventsFor(context: Context, packageName: String){
        Logger.d(TAG, "getting all events stats for $packageName")
        val userManager = context.getSystemService( Context.USER_SERVICE ) as UserManager
        if (userManager.isUserUnlocked.not()){
            return
        }
        withContext(Dispatchers.Default) {
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
                    if (event.packageName != null){
                        allEventList.add(event)
                        Logger.d(TAG, "All events,${event.packageName},${event.className},${event.eventType},${event.timeStamp},${event.configuration}")
                    }
                } catch (packageNotFoundException: PackageManager.NameNotFoundException) {
                    //appUsageMap.remove(event.packageName)
                    continue
                } catch (e: Exception) {
                    e.printStackTrace()
                    continue
                }
            }
        }
    }

}