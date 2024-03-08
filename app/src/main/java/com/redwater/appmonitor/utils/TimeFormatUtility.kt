package com.redwater.appmonitor.utils

import android.os.Build
import com.redwater.appmonitor.data.model.TimeModel
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class TimeFormatUtility{
    val thresholdTimeStringList = listOf<String>("15 Min", "30 Min", "45 Min", "1 Hr", "2 Hrs", "3 Hrs")
    fun getTimeInMin(thresholdTimeInString: String): Short{
        var thresholdTimeInMin: Short = (24*60).toShort()
        try {
            val timeSplit = thresholdTimeInString.split(" ")
            thresholdTimeInMin = if (timeSplit[1].contains("Hr")){
                (timeSplit[0].toShort() * 60).toShort()
            }else{
                timeSplit[0].toShort()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return thresholdTimeInMin
    }

    fun getTimeInMin(timeModel: TimeModel): Short{
        return try {
            val min = (timeModel.hour * 60)+timeModel.minute
            min.toShort()
        }catch (e: Exception){
            e.printStackTrace()
            (24*60).toShort()
        }
    }

    fun getDateTimeFromEpoch(timestamp: Long, format: String = "dd-MM-yyyy hh:mm:ss"): String{
        val formatter = SimpleDateFormat(format, Locale.US)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return formatter.format(calendar.time)
    }

    fun getFormattedTimeString(timeInMin: Short): String{
        val hr = timeInMin/60
        val min = timeInMin%60
        return if (hr > 0){
            if (min > 0){
                "$hr Hr $min Min"
            }else{
                "$hr Hr"
            }
        }else{
            if (min > 0){
                "$min Min"
            }else{
                "NA"
            }
        }
    }

    fun getTimeWithSecString(timeInSec: Long): String{

        val sec = timeInSec%60
        val min = timeInSec/60
        return if (min <= 0){
            "$sec Sec"
        }else{
            if (sec > 0){
                "${getFormattedTimeString(min.toShort())} $sec Sec"
            }else{
                getFormattedTimeString(min.toShort())
            }

        }
    }

    /**
     * Checks if the given time string is within the set DND limit
     * For example, if the string is 10:00 PM and startTime is 09:00 PM and endTime is 06:00 AM, then returns true else false
     * @receiver timeToCheck [String] must be in format "hh:mm a"
     * @param startTimeStr [String] start time of the DND period. must be in format "hh:mm a"
     * @param endTimeStr [String] end time of the DND period. must be in format "hh:mm a"
     *@return [Boolean] whether the timeToCheck falls within DND period
     */

    fun isWithinDNDTime(timeToCheckStr: String, startTimeStr: String, endTimeStr: String): Boolean {
        var isWithinPeriod = false
        try {
                val dateFormat = SimpleDateFormat("hh:mm a", Locale.US)
                val startTime = dateFormat.parse(startTimeStr)
                val endTime = dateFormat.parse(endTimeStr)
                val timeToCheck = dateFormat.parse(timeToCheckStr)

                if (endTime != null && startTime != null && timeToCheck != null) {
                    isWithinPeriod = if (endTime.before(startTime)) {
                        val isOutsideDNDPeriod = timeToCheck.before(startTime) || timeToCheck.after(endTime)
                        !isOutsideDNDPeriod
                    } else {
                        timeToCheck.after(startTime) && timeToCheck.before(endTime)
                    }
                }
        } catch (e: Exception) {
           e.printStackTrace()
        }

        return isWithinPeriod
    }


    fun isWithinDNDTimeV0(timeToCheckStr: String, startTimeStr: String, endTimeStr: String): Boolean{
        var isWithinPeriod = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            val startTime = LocalTime.parse(startTimeStr, formatter)
            val endTime = LocalTime.parse(endTimeStr, formatter)
            val timeToCheck = LocalTime.parse(timeToCheckStr, formatter)

            isWithinPeriod = if (endTime.isBefore(startTime)){
                val inverseDND = timeToCheck.isBefore(startTime) && timeToCheck.isAfter(endTime)
                inverseDND.not()
            }else{
                timeToCheck.isAfter(startTime) && timeToCheck.isBefore(endTime)
            }

        }else{
            val dateFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val startTime = dateFormat.parse(startTimeStr)
            val endTime = dateFormat.parse(endTimeStr)
            val timeToCheck = dateFormat.parse(timeToCheckStr)

            endTime?.let { endT ->
                if (endT.before(startTime)){
                    timeToCheck?.let {checkT->
                        val inverseDND = checkT.before(startTime) && checkT.after(endTime)
                        isWithinPeriod = inverseDND.not()
                    }
                }else{
                    timeToCheck?.let {checkT->
                        isWithinPeriod = checkT.after(startTime) && checkT.before(endTime)
                    }
                }
            }
        }
        return isWithinPeriod
    }

    fun upcomingDNDDelay(timeToCheckStr: String, startTimeStr: String,): Long {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val startTime = dateFormat.parse(startTimeStr)
        val timeToCheck = dateFormat.parse(timeToCheckStr)

        timeToCheck?.let {tCheck->
            if (tCheck.before(startTime)){
                startTime?.let {tStart->
                    val diff = tStart.time - tCheck.time
                    if (diff <= 60*60 && diff > 0){
                        return diff/60
                    }
                }
            }
        }
        return 61
    }
}
