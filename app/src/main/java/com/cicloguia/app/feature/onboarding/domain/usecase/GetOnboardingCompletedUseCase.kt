package com.cicloguia.app.feature.onboarding.domain.usecase

import com.cicloguia.app.feature.onboarding.domain.OnboardingRepository
import javax.inject.Inject

class GetOnboardingCompletedUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    operator fun invoke() = repository.isCompleted()
}