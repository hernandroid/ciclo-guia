package com.cicloguia.app.feature.onboarding.presentation

data class OnboardingUiState(
    val hasLocationPermission: Boolean = false,
    val shouldShowLocationRationale: Boolean = false,
    val isLoading: Boolean = false
)