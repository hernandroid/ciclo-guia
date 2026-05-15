package com.cicloguia.app.feature.appstart.presentation

import androidx.lifecycle.ViewModel
import com.cicloguia.app.feature.onboarding.domain.usecase.GetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppStartViewModel @Inject constructor(
    getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase
) : ViewModel() {

    val hasCompletedOnboarding = getOnboardingCompletedUseCase()
}