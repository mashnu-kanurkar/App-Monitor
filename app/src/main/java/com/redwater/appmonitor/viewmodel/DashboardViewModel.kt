package com.redwater.appmonitor.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.OverlayDataRepository
import kotlinx.coroutines.launch

class DashboardViewModel(private val usageStatsRepository: AppUsageStatsRepository,
                         private val overlayDataRepository: OverlayDataRepository): ViewModel() {

    var quoteWithAuthor = mutableStateOf(Constants.defQuote)
        private set
    init {
        getQuote()
    }
    private fun getQuote(){
        viewModelScope.launch {
            quoteWithAuthor.value = overlayDataRepository.getRandomQuote()
        }
    }
}