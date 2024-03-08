package com.redwater.appmonitor.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redwater.appmonitor.data.model.Blog
import com.redwater.appmonitor.data.model.Quote
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.BlogRepository
import com.redwater.appmonitor.data.repository.QuotesRepository
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(private val usageStatsRepository: AppUsageStatsRepository,
                         private val quotesRepository: QuotesRepository,
                         private val blogRepository: BlogRepository): ViewModel() {
                             private val TAG = this::class.simpleName
    var quote = mutableStateOf<Quote?>(null)
        private set

    var appData = mutableStateMapOf<String, Int>()
        private set

    var blog = mutableStateOf<Blog?>(null)


    fun getQuote(){
        viewModelScope.launch(Dispatchers.Default) {
            quotesRepository.getRandomQuote()
                .flowOn(Dispatchers.Main)
                .collectLatest {
                    quote.value = it
                }
        }
    }

    fun getBlog(){
        viewModelScope.launch(Dispatchers.Default) {
            blogRepository.getRandomBlog()
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    blog.value = it
                }
        }
    }

    fun getUsageDataForChart(context: Context){
        viewModelScope.launch(Dispatchers.Default) {
            val usageStats = usageStatsRepository.getAppModelData(context = context)

            val sortedList = usageStats.values.sortedByDescending { it.usageTimeInMillis }
            Logger.d(TAG, "sorted list,name,usage")
            sortedList.forEach {
                Logger.d(TAG, "sorted list,${it.name},${it.usageTimeInMillis}")
            }

            //get the first top 5 data and sum all remaining
            val allSum = sortedList.sumOf { it.usageTimeInMillis }
            Logger.d(TAG, "All sum $allSum")
            var excludedSum:Long = 0
            val maxAppData = 4
            val tempData = mutableMapOf<String, Int>()
            for(index in 0..maxAppData){
                try {
                    tempData.put(sortedList.get(index).name,
                        ((sortedList.get(index).usageTimeInMillis)/(1000*60)).toInt()
                    )
                    Logger.d(TAG, "adding to the exclusion $excludedSum")
                    excludedSum += sortedList.get(index).usageTimeInMillis
                }catch (e: IndexOutOfBoundsException){
                    Logger.d(TAG, "app count less than 4")
                }

            }
            Logger.d(TAG, "all sum $allSum vs excluded sum $excludedSum")
            if (sortedList.size > maxAppData && excludedSum > 0){
                tempData.put("Other", ((allSum - excludedSum)/(1000*60)).toInt())
            }
            Logger.d(TAG, "temp data: ${tempData.values}")
            withContext(Dispatchers.Main){
                appData.putAll(tempData)
            }
        }
    }
}