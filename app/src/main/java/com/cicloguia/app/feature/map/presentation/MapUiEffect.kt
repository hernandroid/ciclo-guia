package com.cicloguia.app.feature.map.presentation

sealed interface MapUiEffect {
    data object NavigateToReport : MapUiEffect
}