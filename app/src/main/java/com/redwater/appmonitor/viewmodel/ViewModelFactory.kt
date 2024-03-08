package com.redwater.appmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository
import com.redwater.appmonitor.data.repository.BlogRepository
import com.redwater.appmonitor.data.repository.QuotesRepository

class ViewModelFactory(private val preferenceRepository: AppUsageStatsRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(AppUsageStatsRepository::class.java).newInstance(preferenceRepository)
    }
}

class DashboardViewModelFactory(private val usageStatsRepository: AppUsageStatsRepository,
                                private val quotesRepository: QuotesRepository,
                                private val blogRepository: BlogRepository
    ): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(usageStatsRepository = usageStatsRepository,
                quotesRepository = quotesRepository,
                blogRepository = blogRepository
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DNDViewModelFactory(private val usageStatsRepository: AppUsageStatsRepository,
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DNDViewModel::class.java)) {
            return DNDViewModel(usageStatsRepository = usageStatsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}