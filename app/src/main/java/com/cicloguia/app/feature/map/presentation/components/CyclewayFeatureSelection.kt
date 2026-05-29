package com.cicloguia.app.feature.map.presentation.components

import android.graphics.RectF
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.geojson.Feature

internal fun MapLibreMap.registerCyclewayClickListener(
    onFeatureSelected: (Feature) -> Unit,
    onEmptyAreaClicked: () -> Unit
) {
    addOnMapClickListener { latLng ->
        val screenPoint = projection.toScreenLocation(latLng)

        val tapBox = RectF(
            (screenPoint.x - CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.y - CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.x + CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.y + CYCLEWAY_TAP_TOLERANCE_PX).toFloat()
        )

        val selectedFeature = queryRenderedFeatures(
            tapBox,
            CYCLEWAYS_EXISTING_LAYER_ID,
            CYCLEWAYS_PLANNED_LAYER_ID,
            CYCLEWAYS_UNDER_CONSTRUCTION_LAYER_ID
        ).firstOrNull()

        if (selectedFeature != null) {
            onFeatureSelected(selectedFeature)
            true
        } else {
            onEmptyAreaClicked()
            false
        }
    }
}

private const val CYCLEWAY_TAP_TOLERANCE_PX = 32.0