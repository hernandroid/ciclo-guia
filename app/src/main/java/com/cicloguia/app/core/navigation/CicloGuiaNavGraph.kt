package com.cicloguia.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cicloguia.app.feature.map.presentation.MapRoute
import com.cicloguia.app.feature.onboarding.presentation.OnboardingRoute

@Composable
fun CicloGuiaNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Onboarding.route
    ) {
        composable(AppRoute.Onboarding.route) {
            OnboardingRoute(
                onNavigateToHome = {
                    navController.navigate(AppRoute.Map.route) {
                        popUpTo(AppRoute.Onboarding.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoute.Map.route) {
            MapRoute(
                onNavigateToReport = {
                    // Próximo paso: navegar a ReportScreen
                }
            )
        }
    }
}