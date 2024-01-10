package com.redwater.appmonitor.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore("onboarding")

class OnBoardingPreferences(private val context: Context){
    private val TAG = "OnBoardingPreferences"
    private val onboardingCompleted = intPreferencesKey("onboardingCompleted")
    val onboardingCompletedFlow: Flow<Int> = context.dataStore.data.map {
        Logger.d(TAG, "fetching prefs: $it")
        it[onboardingCompleted] ?: 0
    }

    suspend fun updateOnBoardingPreference(isCompleted: Int = 0){
        context.dataStore.edit {
            it[onboardingCompleted] = isCompleted
        }
    }


}
