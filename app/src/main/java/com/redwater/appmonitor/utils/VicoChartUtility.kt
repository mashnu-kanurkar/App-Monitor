package com.redwater.appmonitor.utils

import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import com.redwater.appmonitor.data.model.MonthlyStats
import com.redwater.appmonitor.logger.Logger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class VicoChartUtility {

    private val TAG = "VicoChartUtility"

    fun getEntriesWithLabel(usageDist: Map<Short, Long>): Pair<List<FloatEntry>, List<String>> {
        val floatEntryList = mutableListOf<FloatEntry>()
        val xValuesList = mutableListOf<String>()
        return try {
            usageDist.toSortedMap().toList().forEachIndexed { index, pair ->
                floatEntryList.add(entryOf(x = index.toFloat(), y = pair.second/(60*1000)))
                xValuesList.add(pair.first.toString())
            }
            Pair(first = floatEntryList, second = xValuesList)
        }catch (e: Exception){
            Logger.d(TAG, "Exception: ${e.message}")
            Pair(first = floatEntryList, second = xValuesList)
        }

    }

    fun getMonthlyEntriesWithLabel(monthlyStatsList: List<MonthlyStats>): Pair<MutableList<FloatEntry>, MutableList<String>> {
        val floatEntryList = mutableListOf<FloatEntry>()
        val xValuesList = mutableListOf<String>()
        return try {
            monthlyStatsList.forEachIndexed { index, monthlyStats ->
                floatEntryList.add(entryOf(x = index.toFloat(), y = monthlyStats.totalTime/(60*1000)))
                val date = Date(monthlyStats.firstTs)
                val dateFormat = SimpleDateFormat("dd MMM")
                val formattedDate = dateFormat.format(date)
                xValuesList.add(formattedDate)
            }
            Pair(first = floatEntryList, second = xValuesList)
        }catch (e: Exception){
            Logger.d(TAG, "Exception: ${e.message}")
            Pair(first = floatEntryList, second = xValuesList)
        }

    }
    fun getEntryModel(usageDist: Map<Short, Long>): ChartEntryModel {
        return usageDist.toSortedMap()
            .map { (x, y) -> entryOf(x.toFloat(), (y/60).toFloat()) }
            .let { entryList -> ChartEntryModelProducer(listOf(entryList)) }
            .getModel()
    }
}