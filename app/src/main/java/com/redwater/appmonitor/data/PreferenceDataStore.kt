package com.redwater.appmonitor.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.redwater.appmonitor.logger.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userPreference")

class UserPreferences(private val context: Context){
    private val TAG = "UserPreferences"
    private val onboardingCompleted = intPreferencesKey("onboardingCompleted")
    val onboardingCompletedFlow: Flow<Int> = context.dataStore.data.map {
        Logger.d(TAG, "providing onboarding prefs: $it")
        it[onboardingCompleted] ?: 0
    }

    suspend fun updateOnBoardingPreference(isCompleted: Int = 0){
        Logger.d(TAG, "updating onboarding prefs: $isCompleted")
        context.dataStore.edit {
            it[onboardingCompleted] = isCompleted
        }
    }
    private val privacyPolicyAcceptKey = intPreferencesKey("privacyPolicyAccepted")
    val privacyPolicyAcceptFlow: Flow<Int> = context.dataStore.data.map {
        Logger.d(TAG, "providing privacy policy prefs: $it")
        it[privacyPolicyAcceptKey] ?: 0
    }

    suspend fun updatePrivacyPolicyAcceptPrefs(isAccepted: Int = 0){
        Logger.d(TAG, "updating privacy policy prefs: $isAccepted")
        context.dataStore.edit {
            it[privacyPolicyAcceptKey] = isAccepted
        }
    }


}
