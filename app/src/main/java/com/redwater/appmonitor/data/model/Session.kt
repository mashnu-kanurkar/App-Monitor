package com.redwater.appmonitor.data.model

import com.redwater.appmonitor.logger.Logger
import java.util.Calendar

data class SessionData(val sessionId: Long, var sessionLength: Long, var lastSessionEndTS: Long)

class Session(start: Long, end: Long){

    private var _sessionList: MutableList<SessionData> = mutableListOf()
    val sessionList = _sessionList
    init {
        newSession(start = start, end = end)
    }
    fun addSession(start: Long, end: Long){
        if (_sessionList.size == 0){
            newSession(start = start, end = end)
        }else{
            val lastIndex = _sessionList.size - 1
            if ((start - _sessionList[lastIndex].lastSessionEndTS) > 5000){
                newSession(start = start, end = end)
            }else{
                updateSession(start = start, end = end, index = lastIndex)
            }
        }
    }

    private fun updateSession(start: Long, end: Long, index: Int){
        val oldSessionLength = _sessionList[index].sessionLength
        _sessionList[index].apply {
            this.lastSessionEndTS = end
            this.sessionLength = oldSessionLength + (end - start)
        }

    }
    private fun newSession(start: Long, end: Long){
        _sessionList.add(SessionData(sessionId = start, sessionLength = end - start, lastSessionEndTS = end))
    }

    override fun toString(): String {
        return _sessionList.toString()
    }
}

fun Session.maxOrNull(): SessionData?{
    return this.sessionList.maxByOrNull {
        it.sessionLength
    }
}

fun Session.hourlyDistributionInMillis():Map<Short, Long>{
    Logger.d("Session analytics", "converting to hourly distribution")
    val usageDist = mutableMapOf<Short, Long>()
    val calendar: Calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.set(Calendar.AM_PM, Calendar.AM)
    var startIndex = 0
    var carryOverLength = 0L
    val sessionList = this.sessionList
    while (startIndex <= sessionList.size -1){
        try {
            val hr0 = (sessionList[startIndex].sessionId- calendar.timeInMillis)/(1000*60*60)
            val sessionLength = nextSessionLength(sessionList = sessionList, index = startIndex, baseTimeInMillis = calendar.timeInMillis)
            usageDist.put(key = hr0.toShort(), value = sessionLength.length + carryOverLength)
            carryOverLength = sessionLength.carryOverLength
            startIndex = sessionLength.index+1
        }catch (e: IndexOutOfBoundsException){
            return usageDist
        }

    }
    return usageDist
}

//length, carryOverLength, iterationIndex
fun nextSessionLength(sessionList: List<SessionData>, index: Int, baseTimeInMillis: Long): SessionDistributionHelper{
    val hr0 = (sessionList[index].sessionId-baseTimeInMillis)/(1000*60*60)
    //val postHourSecondsInMilli = (sessionList[index].sessionId)%(60*1000*60)
    val tempCalender = Calendar.getInstance()
    tempCalender.timeInMillis = sessionList[index].sessionId
    val postMin = tempCalender.get(Calendar.MINUTE)
    val postSec = tempCalender.get(Calendar.SECOND)
    val postMilliSec = tempCalender.get(Calendar.MILLISECOND)
    val postHourSecondsInMilli: Long = ((postMin*60*1000)+(postSec*1000)+(postMilliSec)).toLong()
    val millisTillNextHour = (3600000 - postHourSecondsInMilli)
    var currentSessionLength = sessionList[index].sessionLength
    var carryOver = 0L
    if (currentSessionLength > millisTillNextHour){
        carryOver = currentSessionLength - millisTillNextHour
        currentSessionLength = millisTillNextHour
    }
    return try {
        val hr1 = (sessionList[index + 1].sessionId-baseTimeInMillis)/(1000*60*60)
        if (hr0 == hr1){
            val next = nextSessionLength(sessionList = sessionList, index = index+1, baseTimeInMillis = baseTimeInMillis)
            SessionDistributionHelper(length = (currentSessionLength + next.length), carryOverLength = (carryOver + next.carryOverLength), index = next.index )
        }else{
            SessionDistributionHelper(length = currentSessionLength, carryOverLength = carryOver, index = index )
        }
    }catch (e: IndexOutOfBoundsException){
        SessionDistributionHelper(length = currentSessionLength, carryOverLength = carryOver, index = index )
    }
}

data class SessionDistributionHelper(val length: Long, val carryOverLength: Long, val index: Int)
