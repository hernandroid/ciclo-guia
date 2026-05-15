package com.cicloguia.app.core.navigation

sealed class AppRoute(val route: String) {
    data object AppStart : AppRoute("app_start")
    data object Onboarding : AppRoute("onboarding")
    data object Map : AppRoute("map")
}