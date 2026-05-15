package com.cicloguia.app.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferencesKeys {
    val HasCompletedOnboarding = booleanPreferencesKey("has_completed_onboarding")
}