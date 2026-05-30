package com.cicloguia.app.feature.map.presentation

object MapReportBetaBehavior {

    fun reportClickedEffect(): MapUiEffect {
        return MapUiEffect.ShowMessage(REPORT_UPCOMING_MESSAGE)
    }

    const val REPORT_UPCOMING_MESSAGE =
        "La función para reportar ciclovías estará disponible próximamente."
}
