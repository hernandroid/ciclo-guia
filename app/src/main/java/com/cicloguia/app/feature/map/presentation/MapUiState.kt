package com.cicloguia.app.feature.map.presentation

import com.cicloguia.app.feature.map.presentation.model.CyclewayLegendUi
import com.cicloguia.app.feature.map.presentation.model.SelectedCyclewayUi

sealed interface MapUiState {

    data object Loading : MapUiState

    data class Content(
        val geoJson: String,
        val mapStyleUrl: String,
        val selectedCyclewayName: String = "Ciclovías de Lima",
        val selectedCycleway: SelectedCyclewayUi? = null,
        val isSyncing: Boolean = false,
        val centerOnUserLocationRequest: Int = 0,
        val isFollowingUserLocation: Boolean = false,
        val legend: CyclewayLegendUi = CyclewayLegendUi()
    ) : MapUiState

    data class Error(
        val message: String
    ) : MapUiState
}