package com.cicloguia.app.feature.onboarding.presentation

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingRoute(
    onNavigateToHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status) {
        viewModel.onEvent(
            OnboardingUiEvent.LocationPermissionChanged(
                granted = locationPermissionState.status.isGranted,
                shouldShowRationale = locationPermissionState.status.shouldShowRationale
            )
        )
    }

    LaunchedEffect(viewModel.navigateToHome) {
        if (viewModel.navigateToHome) {
            onNavigateToHome()
            viewModel.onNavigationHandled()
        }
    }

    OnboardingScreen(
        uiState = viewModel.uiState,
        onRequestLocationPermission = {
            locationPermissionState.launchPermissionRequest()
        },
        onContinueWithoutLocation = {
            viewModel.onEvent(OnboardingUiEvent.ContinueWithoutLocation)
        }
    )
}