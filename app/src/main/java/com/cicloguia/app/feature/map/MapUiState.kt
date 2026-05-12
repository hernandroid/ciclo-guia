package com.cicloguia.app.feature.map

sealed interface MapUiState {
    data object Loading : MapUiState

    data class Success(
        val geoJson: String,
        val selectedCyclewayName: String = "Ciclovías de Lima",
        val isSyncing: Boolean = false
    ) : MapUiState

    data class Error(
        val message: String
    ) : MapUiState
}