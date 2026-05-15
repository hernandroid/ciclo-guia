package com.cicloguia.app.feature.onboarding.presentation

sealed interface OnboardingUiEvent {
    data object ContinueWithoutLocation : OnboardingUiEvent

    data class LocationPermissionChanged(
        val granted: Boolean,
        val shouldShowRationale: Boolean
    ) : OnboardingUiEvent
}