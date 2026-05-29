package com.cicloguia.app.feature.map.presentation.components

import com.cicloguia.app.feature.map.presentation.model.CyclewayGeoJsonProperty
import com.cicloguia.app.feature.map.presentation.model.CyclewayMapColor
import com.cicloguia.app.feature.map.presentation.model.CyclewayStatus
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.eq
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineDasharray
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

internal fun updateCyclewayLayers(
    map: MapLibreMap,
    geoJson: String
) {
    map.style?.let { style ->
        addOrUpdateCyclewayLayers(
            style = style,
            geoJson = geoJson
        )
    }
}

internal fun addOrUpdateCyclewayLayers(
    style: Style,
    geoJson: String
) {
    val source = style.getSource(CYCLEWAYS_SOURCE_ID) as? GeoJsonSource

    if (source != null) {
        source.setGeoJson(geoJson)
    } else {
        style.addSource(
            GeoJsonSource(
                CYCLEWAYS_SOURCE_ID,
                geoJson
            )
        )
    }

    style.addHighlightedCyclewaySourceIfNeeded()

    baseLayerConfigs.forEach { config ->
        style.addCyclewayLineLayerIfNeeded(
            config = config,
            sourceId = CYCLEWAYS_SOURCE_ID,
            opacity = BASE_LAYER_OPACITY
        )
    }

    highlightedLayerConfigs.forEach { config ->
        style.addCyclewayLineLayerIfNeeded(
            config = config,
            sourceId = HIGHLIGHTED_CYCLEWAY_SOURCE_ID,
            opacity = HIGHLIGHTED_LAYER_OPACITY
        )
    }
}

internal fun Style.updateHighlightedCyclewayFeature(feature: Feature) {
    val source = getSource(HIGHLIGHTED_CYCLEWAY_SOURCE_ID) as? GeoJsonSource ?: return

    source.setGeoJson(
        FeatureCollection.fromFeatures(
            listOf(feature)
        )
    )
}

internal fun Style.clearHighlightedCyclewayFeature() {
    val source = getSource(HIGHLIGHTED_CYCLEWAY_SOURCE_ID) as? GeoJsonSource ?: return

    source.setGeoJson(
        FeatureCollection.fromFeatures(
            emptyList()
        )
    )
}

private fun Style.addHighlightedCyclewaySourceIfNeeded() {
    val source = getSource(HIGHLIGHTED_CYCLEWAY_SOURCE_ID) as? GeoJsonSource

    if (source == null) {
        addSource(
            GeoJsonSource(
                HIGHLIGHTED_CYCLEWAY_SOURCE_ID,
                FeatureCollection.fromFeatures(emptyList())
            )
        )
    }
}

private fun Style.addCyclewayLineLayerIfNeeded(
    config: CyclewayLayerConfig,
    sourceId: String,
    opacity: Float
) {
    if (getLayer(config.layerId) != null) return

    val layer = LineLayer(config.layerId, sourceId)
        .withProperties(
            lineColor(config.color),
            lineWidth(config.width),
            lineOpacity(opacity),
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND)
        )
        .withFilter(
            eq(
                get(CyclewayGeoJsonProperty.STATUS),
                literal(config.status)
            )
        )

    if (config.dashed) {
        layer.withProperties(
            lineDasharray(arrayOf(DASH_LENGTH, DASH_GAP))
        )
    }

    addLayer(layer)
}

private data class CyclewayLayerConfig(
    val layerId: String,
    val status: String,
    val color: String,
    val width: Float,
    val dashed: Boolean = false
)

internal const val CYCLEWAYS_EXISTING_LAYER_ID = "cicloguia-cycleways-existing-layer"
internal const val CYCLEWAYS_PLANNED_LAYER_ID = "cicloguia-cycleways-planned-layer"
internal const val CYCLEWAYS_UNDER_CONSTRUCTION_LAYER_ID =
    "cicloguia-cycleways-under-construction-layer"

private const val CYCLEWAYS_SOURCE_ID = "cicloguia-cycleways-source"
private const val HIGHLIGHTED_CYCLEWAY_SOURCE_ID = "cicloguia-highlighted-cycleway-source"

private const val HIGHLIGHTED_EXISTING_LAYER_ID = "cicloguia-highlighted-existing-layer"
private const val HIGHLIGHTED_PLANNED_LAYER_ID = "cicloguia-highlighted-planned-layer"
private const val HIGHLIGHTED_UNDER_CONSTRUCTION_LAYER_ID =
    "cicloguia-highlighted-under-construction-layer"

private const val BASE_EXISTING_WIDTH = 2.8f
private const val BASE_PLANNED_WIDTH = 2.6f
private const val BASE_UNDER_CONSTRUCTION_WIDTH = 3.0f

private const val HIGHLIGHTED_EXISTING_WIDTH = 6.0f
private const val HIGHLIGHTED_PLANNED_WIDTH = 6.0f
private const val HIGHLIGHTED_UNDER_CONSTRUCTION_WIDTH = 6.5f

private const val BASE_LAYER_OPACITY = 0.95f
private const val HIGHLIGHTED_LAYER_OPACITY = 1.0f

private const val DASH_LENGTH = 2f
private const val DASH_GAP = 2f

private val baseLayerConfigs = listOf(
    CyclewayLayerConfig(
        layerId = CYCLEWAYS_EXISTING_LAYER_ID,
        status = CyclewayStatus.EXISTING,
        color = CyclewayMapColor.EXISTING_HEX,
        width = BASE_EXISTING_WIDTH
    ),
    CyclewayLayerConfig(
        layerId = CYCLEWAYS_PLANNED_LAYER_ID,
        status = CyclewayStatus.PLANNED,
        color = CyclewayMapColor.PLANNED_HEX,
        width = BASE_PLANNED_WIDTH,
        dashed = true
    ),
    CyclewayLayerConfig(
        layerId = CYCLEWAYS_UNDER_CONSTRUCTION_LAYER_ID,
        status = CyclewayStatus.UNDER_CONSTRUCTION,
        color = CyclewayMapColor.UNDER_CONSTRUCTION_HEX,
        width = BASE_UNDER_CONSTRUCTION_WIDTH
    )
)

private val highlightedLayerConfigs = listOf(
    CyclewayLayerConfig(
        layerId = HIGHLIGHTED_EXISTING_LAYER_ID,
        status = CyclewayStatus.EXISTING,
        color = CyclewayMapColor.EXISTING_HEX,
        width = HIGHLIGHTED_EXISTING_WIDTH
    ),
    CyclewayLayerConfig(
        layerId = HIGHLIGHTED_PLANNED_LAYER_ID,
        status = CyclewayStatus.PLANNED,
        color = CyclewayMapColor.PLANNED_HEX,
        width = HIGHLIGHTED_PLANNED_WIDTH,
        dashed = true
    ),
    CyclewayLayerConfig(
        layerId = HIGHLIGHTED_UNDER_CONSTRUCTION_LAYER_ID,
        status = CyclewayStatus.UNDER_CONSTRUCTION,
        color = CyclewayMapColor.UNDER_CONSTRUCTION_HEX,
        width = HIGHLIGHTED_UNDER_CONSTRUCTION_WIDTH
    )
)