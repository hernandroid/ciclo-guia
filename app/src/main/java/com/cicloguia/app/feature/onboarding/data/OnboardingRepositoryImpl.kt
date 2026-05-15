package com.cicloguia.app.feature.onboarding.data

import com.cicloguia.app.core.datastore.UserPreferencesDataSource
import com.cicloguia.app.feature.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource
) : OnboardingRepository {

    override fun isCompleted(): Flow<Boolean> {
        return dataSource.hasCompletedOnboarding
    }

    override suspend fun saveCompleted() {
        dataSource.saveOnboardingCompleted()
    }
}