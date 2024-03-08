package com.redwater.appmonitor.data.model

import com.redwater.appmonitor.logger.Logger

data class TimeModel(val hour: Int = 0, val minute: Int = 0, val sec: Int = 0, val period: Period? = null)

data class DNDTime(val startTime: TimeModel, val endTime: TimeModel, val appModelMap: MutableMap<String, AppModel> = mutableMapOf())

enum class DNDTimeType{
    START,
    END
}
enum class Period{
    AM,
    PM
}

fun TimeModel.toFormattedString(): String{
    val hour: String = if (this.hour < 10){
        "0${this.hour}"
    }else{
        "${this.hour}"
    }
    val min: String = if (this.minute < 10){
        "0${this.minute}"
    }else{
        "${this.minute}"
    }
    return "$hour:$min ${this.period}"
}
fun String.toTimeModel(): TimeModel?{
    Logger.d("toTimeModel", "time string: $this")
    val (hour, split1) = this.split(":")
    val (min, period) = split1.split(" ")
    return try {
        val hourInt = hour.toInt()
        val minInt = min.toInt()
        val periodEnum = if (period == "AM") Period.AM else Period.PM
        TimeModel(hour = hourInt, minute = minInt, period = periodEnum)
    }catch (e: Exception){
        e.printStackTrace()
        null
    }
}

fun createDNDKey(startTime: TimeModel, endTime: TimeModel): String{
    return "${startTime.toFormattedString()}_${endTime.toFormattedString()}"
}
fun createDNDKey(startTime: String, endTime: String): String{
    return "${startTime}_${endTime}"
}
fun getTimeModelFromKey(dndKey: String): Pair<TimeModel, TimeModel>?{
    Logger.d("getTimeModelFromKey", "dndKey = ${dndKey}")
    val (startTimeString, endTimeString) = dndKey.split("_")
    val startTime = startTimeString.toTimeModel()
    val endTime = endTimeString.toTimeModel()
    var timeModelPair: Pair<TimeModel, TimeModel>? = null
    startTime?.let {start->
        endTime?.let {end->
            timeModelPair = Pair(start, end)
        }
    }
    return timeModelPair
}

fun getDNDTimeFrom(entry: Map.Entry<String, MutableMap<String, AppModel>>): DNDTime?{
    Logger.d("getDNDTimeFrom", "dndKey = ${entry.key}")
    val timePair = getTimeModelFromKey(entry.key)
    val startTime = timePair?.first
    val endTime = timePair?.second
    startTime?.let {start->
        endTime?.let {end->
           return DNDTime(startTime = start, endTime = end, appModelMap = entry.value)
        }
    }
    return null
}

fun List<AppModel>.toDNDMap():MutableMap<String, MutableMap<String, AppModel>>{
    Logger.d("TimeModel", "List to DND ${this.toString()}")
    val dndMap = mutableMapOf<String, MutableMap<String, AppModel>>()
    this.forEach {appModel->
        appModel.dndStartTime?.let { startTimeModel ->
                appModel.dndEndTime?.let { endTimeModel ->
                    val dndKey = createDNDKey(startTimeModel, endTimeModel)
                    val existingAppMap = dndMap.get(dndKey)
                    existingAppMap?.let {
                        it.put(appModel.packageName, appModel)
                        dndMap.put(dndKey, it)
                    }?:run{
                        val newMap = mutableMapOf(appModel.packageName to appModel)
                        dndMap.put(dndKey, newMap)
                    }
                }
            }
    }
    return dndMap
}

