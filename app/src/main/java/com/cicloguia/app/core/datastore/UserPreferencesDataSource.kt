package com.cicloguia.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    @UserPreferencesDataStore
    private val dataStore: DataStore<Preferences>
) {

    val hasCompletedOnboarding: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HasCompletedOnboarding] ?: false
        }

    suspend fun saveOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HasCompletedOnboarding] = true
        }
    }
}