package com.cicloguia.app.feature.onboarding.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cicloguia.app.feature.onboarding.domain.usecase.SaveOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveOnboardingCompletedUseCase: SaveOnboardingCompletedUseCase
) : ViewModel() {

    var uiState by mutableStateOf(OnboardingUiState())
        private set

    var navigateToHome by mutableStateOf(false)
        private set

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.ContinueWithoutLocation -> {
                completeOnboarding()
            }

            is OnboardingUiEvent.LocationPermissionChanged -> {
                uiState = uiState.copy(
                    hasLocationPermission = event.granted,
                    shouldShowLocationRationale = event.shouldShowRationale
                )

                if (event.granted) {
                    completeOnboarding()
                }
            }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            saveOnboardingCompletedUseCase()
            uiState = uiState.copy(isLoading = false)
            navigateToHome = true
        }
    }

    fun onNavigationHandled() {
        navigateToHome = false
    }
}