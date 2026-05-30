package com.cicloguia.app.feature.map.presentation

import com.cicloguia.app.feature.map.presentation.model.CyclewayDestinationUi

sealed interface MapUiEffect {
    data class OpenExternalNavigation(
        val destination: CyclewayDestinationUi
    ) : MapUiEffect

    data class ShowMessage(
        val message: String
    ) : MapUiEffect
}
