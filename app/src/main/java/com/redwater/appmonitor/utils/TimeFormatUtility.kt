package com.redwater.appmonitor.utils

import java.text.SimpleDateFormat
import java.util.Calendar

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

    fun getDateTimeFromEpoch(timestamp: Long, format: String = "dd-MM-yyyy hh:mm:ss"): String{
        val formatter = SimpleDateFormat(format)
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
}