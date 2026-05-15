package com.cicloguia.app.feature.onboarding.domain.usecase

import com.cicloguia.app.feature.onboarding.domain.OnboardingRepository
import javax.inject.Inject

class SaveOnboardingCompletedUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() {
        repository.saveCompleted()
    }
}