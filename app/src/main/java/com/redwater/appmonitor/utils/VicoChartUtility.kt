package com.redwater.appmonitor.utils

import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryOf

class VicoChartUtility {

    fun getEntriesWithLabel(usageDist: Map<Short, Long>): Pair<List<FloatEntry>, List<String>> {
        val floatEntryList = mutableListOf<FloatEntry>()
        val xValuesList = mutableListOf<String>()
        usageDist.toSortedMap().toList().forEachIndexed { index, pair ->
            floatEntryList.add(entryOf(x = index.toFloat(), y = pair.second/(60*1000)))
            xValuesList.add(pair.first.toString())
        }
        return Pair(first = floatEntryList, second = xValuesList)
    }
    fun getEntryModel(usageDist: Map<Short, Long>): ChartEntryModel {
        return usageDist.toSortedMap()
            .map { (x, y) -> entryOf(x.toFloat(), (y/60).toFloat()) }
            .let { entryList -> ChartEntryModelProducer(listOf(entryList)) }
            .getModel()
    }
}