package com.cicloguia.app.feature.appstart.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppStartRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMap: () -> Unit,
    viewModel: AppStartViewModel = hiltViewModel()
) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding
        .collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(hasCompletedOnboarding) {
        when (hasCompletedOnboarding) {
            true -> onNavigateToMap()
            false -> onNavigateToOnboarding()
            null -> Unit
        }
    }
}