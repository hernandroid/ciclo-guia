package com.cicloguia.app.feature.onboarding.domain

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun isCompleted(): Flow<Boolean>
    suspend fun saveCompleted()
}