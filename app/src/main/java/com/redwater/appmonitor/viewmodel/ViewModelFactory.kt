package com.redwater.appmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.redwater.appmonitor.data.repository.AppUsageStatsRepository

class ViewModelFactory(private val preferenceRepository: AppUsageStatsRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(AppUsageStatsRepository::class.java).newInstance(preferenceRepository)
    }
}