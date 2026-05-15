package com.cicloguia.app.feature.map.presentation

sealed interface MapUiState {

    data object Loading : MapUiState

    data class Content(
        val geoJson: String,
        val mapStyleUrl: String,
        val selectedCyclewayName: String = "Ciclovías de Lima",
        val isSyncing: Boolean = false
    ) : MapUiState

    data class Error(
        val message: String
    ) : MapUiState
}