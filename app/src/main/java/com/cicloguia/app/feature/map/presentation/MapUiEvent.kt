package com.cicloguia.app.feature.map.presentation

sealed interface MapUiEvent {
    data object ReportClicked : MapUiEvent
    data object RetryClicked : MapUiEvent
}