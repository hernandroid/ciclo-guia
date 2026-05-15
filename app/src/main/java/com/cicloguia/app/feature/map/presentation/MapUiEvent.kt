package com.cicloguia.app.feature.map.presentation

import com.cicloguia.app.feature.map.presentation.model.SelectedCyclewayUi

sealed interface MapUiEvent {
    data object ReportClicked : MapUiEvent
    data object RetryClicked : MapUiEvent
    data object CenterOnUserLocationClicked : MapUiEvent
    data object DismissSelectedCycleway : MapUiEvent

    data class CyclewayClicked(
        val cycleway: SelectedCyclewayUi
    ) : MapUiEvent
}