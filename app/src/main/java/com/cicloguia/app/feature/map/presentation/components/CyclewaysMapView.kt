package com.cicloguia.app.feature.map.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cicloguia.app.feature.map.presentation.model.SelectedCyclewayUi
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
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
import org.maplibre.geojson.LineString
import org.maplibre.geojson.MultiLineString
import org.maplibre.geojson.Point

private const val CYCLEWAYS_SOURCE_ID = "cicloguia-cycleways-source"

private const val CYCLEWAYS_EXISTING_LAYER_ID = "cicloguia-cycleways-existing-layer"
private const val CYCLEWAYS_PLANNED_LAYER_ID = "cicloguia-cycleways-planned-layer"
private const val CYCLEWAYS_UNDER_CONSTRUCTION_LAYER_ID =
    "cicloguia-cycleways-under-construction-layer"

private const val SELECTED_CYCLEWAY_SOURCE_ID = "cicloguia-selected-cycleway-source"

private const val SELECTED_EXISTING_LAYER_ID = "cicloguia-selected-existing-layer"
private const val SELECTED_PLANNED_LAYER_ID = "cicloguia-selected-planned-layer"
private const val SELECTED_UNDER_CONSTRUCTION_LAYER_ID =
    "cicloguia-selected-under-construction-layer"

private const val STATUS_EXISTING = "EXISTENTE"
private const val STATUS_PLANNED = "EN PROYECTO"
private const val STATUS_UNDER_CONSTRUCTION = "EN EJECUCIÓN"

private const val COLOR_EXISTING = "#00B37A"
private const val COLOR_PLANNED = "#FF7A00"
private const val COLOR_UNDER_CONSTRUCTION = "#1976D2"

private const val UNKNOWN_VALUE = "No especificado"
private const val CYCLEWAY_TAP_TOLERANCE_PX = 32.0
private const val CAMERA_ANIMATION_DURATION_MS = 900

private const val EMPTY_FEATURE_COLLECTION = """
{
  "type": "FeatureCollection",
  "features": []
}
"""

private var selectedMapFeature: Feature? = null
private var lastHandledCameraFitRequest: Int = 0

@Composable
fun CyclewaysMapView(
    styleUrl: String,
    geoJson: String,
    hasLocationPermission: Boolean,
    centerOnUserLocationRequest: Int,
    selectedCycleway: SelectedCyclewayUi?,
    selectedCyclewayCameraFitRequest: Int,
    bottomSheetHeightPx: Int,
    onCameraCenteredOnUserLocation: () -> Unit,
    onMapMovedByUser: () -> Unit,
    onCyclewayClick: (SelectedCyclewayUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    setupMap(
                        context = context,
                        map = map,
                        styleUrl = styleUrl,
                        geoJson = geoJson,
                        hasLocationPermission = hasLocationPermission,
                        onCameraCenteredOnUserLocation = onCameraCenteredOnUserLocation,
                        onCyclewayClick = onCyclewayClick,
                        onMapMovedByUser = onMapMovedByUser
                    )
                }
            }
        },
        update = {
            it.getMapAsync { map ->
                updateCyclewaysLayer(
                    map = map,
                    geoJson = geoJson
                )

                if (selectedCycleway == null) {
                    selectedMapFeature = null
                    lastHandledCameraFitRequest = 0

                    map.style?.let { style ->
                        clearSelectedCyclewayFeature(style)
                    }
                }

                if (
                    selectedCycleway != null &&
                    selectedCyclewayCameraFitRequest > 0 &&
                    selectedCyclewayCameraFitRequest != lastHandledCameraFitRequest &&
                    bottomSheetHeightPx > 0
                ) {
                    fitCameraToSelectedFeature(
                        map = map,
                        bottomSheetHeightPx = bottomSheetHeightPx
                    )

                    lastHandledCameraFitRequest = selectedCyclewayCameraFitRequest
                }

                if (hasLocationPermission && centerOnUserLocationRequest > 0) {
                    centerCameraOnUserLocation(
                        map = map,
                        onCameraCenteredOnUserLocation = onCameraCenteredOnUserLocation
                    )
                }
            }
        }
    )
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return mapView
}

private fun setupMap(
    context: Context,
    map: MapLibreMap,
    styleUrl: String,
    geoJson: String,
    hasLocationPermission: Boolean,
    onCameraCenteredOnUserLocation: () -> Unit,
    onMapMovedByUser: () -> Unit,
    onCyclewayClick: (SelectedCyclewayUi) -> Unit
) {
    map.uiSettings.isLogoEnabled = false
    map.uiSettings.isAttributionEnabled = false

    map.cameraPosition = CameraPosition.Builder()
        .target(LatLng(-12.0945, -77.0445))
        .zoom(12.0)
        .build()

    map.setStyle(
        Style.Builder().fromUri(styleUrl)
    ) { style ->
        addOrUpdateCyclewaysLayer(
            style = style,
            geoJson = geoJson
        )

        setupCyclewayClickListener(
            map = map,
            onCyclewayClick = onCyclewayClick
        )

        map.addOnCameraMoveStartedListener { reason ->
            if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                onMapMovedByUser()
            }
        }

        if (hasLocationPermission) {
            enableUserLocationAndCenterOnce(
                context = context,
                map = map,
                style = style,
                onCameraCenteredOnUserLocation = onCameraCenteredOnUserLocation
            )
        }
    }
}

private fun updateCyclewaysLayer(
    map: MapLibreMap,
    geoJson: String
) {
    map.style?.let { style ->
        addOrUpdateCyclewaysLayer(
            style = style,
            geoJson = geoJson
        )
    }
}

private fun addOrUpdateCyclewaysLayer(
    style: Style,
    geoJson: String
) {
    val existingSource = style.getSource(CYCLEWAYS_SOURCE_ID) as? GeoJsonSource

    if (existingSource != null) {
        existingSource.setGeoJson(geoJson)
    } else {
        style.addSource(
            GeoJsonSource(
                CYCLEWAYS_SOURCE_ID,
                geoJson
            )
        )
    }

    addSelectedCyclewaySourceIfNeeded(style)

    addCyclewayLayerIfNeeded(
        style = style,
        layerId = CYCLEWAYS_EXISTING_LAYER_ID,
        status = STATUS_EXISTING,
        color = COLOR_EXISTING,
        width = 2.8f
    )

    addCyclewayLayerIfNeeded(
        style = style,
        layerId = CYCLEWAYS_PLANNED_LAYER_ID,
        status = STATUS_PLANNED,
        color = COLOR_PLANNED,
        width = 2.6f,
        dashed = true
    )

    addCyclewayLayerIfNeeded(
        style = style,
        layerId = CYCLEWAYS_UNDER_CONSTRUCTION_LAYER_ID,
        status = STATUS_UNDER_CONSTRUCTION,
        color = COLOR_UNDER_CONSTRUCTION,
        width = 3.0f
    )

    addSelectedCyclewayLayerIfNeeded(
        style = style,
        layerId = SELECTED_EXISTING_LAYER_ID,
        status = STATUS_EXISTING,
        color = COLOR_EXISTING,
        width = 6.0f
    )

    addSelectedCyclewayLayerIfNeeded(
        style = style,
        layerId = SELECTED_PLANNED_LAYER_ID,
        status = STATUS_PLANNED,
        color = COLOR_PLANNED,
        width = 6.0f,
        dashed = true
    )

    addSelectedCyclewayLayerIfNeeded(
        style = style,
        layerId = SELECTED_UNDER_CONSTRUCTION_LAYER_ID,
        status = STATUS_UNDER_CONSTRUCTION,
        color = COLOR_UNDER_CONSTRUCTION,
        width = 6.5f
    )
}

private fun addSelectedCyclewaySourceIfNeeded(
    style: Style
) {
    val selectedSource = style.getSource(SELECTED_CYCLEWAY_SOURCE_ID) as? GeoJsonSource

    if (selectedSource == null) {
        style.addSource(
            GeoJsonSource(
                SELECTED_CYCLEWAY_SOURCE_ID,
                EMPTY_FEATURE_COLLECTION
            )
        )
    }
}

private fun addCyclewayLayerIfNeeded(
    style: Style,
    layerId: String,
    status: String,
    color: String,
    width: Float,
    dashed: Boolean = false
) {
    if (style.getLayer(layerId) != null) return

    val layer = LineLayer(layerId, CYCLEWAYS_SOURCE_ID)
        .withProperties(
            lineColor(color),
            lineWidth(width),
            lineOpacity(0.95f),
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND)
        )
        .withFilter(
            eq(
                get("ESTADO"),
                literal(status)
            )
        )

    if (dashed) {
        layer.withProperties(
            lineDasharray(arrayOf(2f, 2f))
        )
    }

    style.addLayer(layer)
}

private fun addSelectedCyclewayLayerIfNeeded(
    style: Style,
    layerId: String,
    status: String,
    color: String,
    width: Float,
    dashed: Boolean = false
) {
    if (style.getLayer(layerId) != null) return

    val layer = LineLayer(layerId, SELECTED_CYCLEWAY_SOURCE_ID)
        .withProperties(
            lineColor(color),
            lineWidth(width),
            lineOpacity(1.0f),
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND)
        )
        .withFilter(
            eq(
                get("ESTADO"),
                literal(status)
            )
        )

    if (dashed) {
        layer.withProperties(
            lineDasharray(arrayOf(2f, 2f))
        )
    }

    style.addLayer(layer)
}

private fun setupCyclewayClickListener(
    map: MapLibreMap,
    onCyclewayClick: (SelectedCyclewayUi) -> Unit
) {
    map.addOnMapClickListener { latLng ->
        val screenPoint = map.projection.toScreenLocation(latLng)

        val tapBox = RectF(
            (screenPoint.x - CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.y - CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.x + CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.y + CYCLEWAY_TAP_TOLERANCE_PX).toFloat()
        )

        val features = map.queryRenderedFeatures(
            tapBox,
            CYCLEWAYS_EXISTING_LAYER_ID,
            CYCLEWAYS_PLANNED_LAYER_ID,
            CYCLEWAYS_UNDER_CONSTRUCTION_LAYER_ID
        ).toList()

        val selectedFeature = features.firstOrNull()

        if (selectedFeature != null) {
            selectedMapFeature = selectedFeature
            lastHandledCameraFitRequest = 0

            map.style?.let { style ->
                updateSelectedCyclewayFeature(
                    style = style,
                    feature = selectedFeature
                )
            }

            onCyclewayClick(selectedFeature.toSelectedCyclewayUi())
            true
        } else {
            selectedMapFeature = null
            lastHandledCameraFitRequest = 0

            map.style?.let { style ->
                clearSelectedCyclewayFeature(style)
            }

            false
        }
    }
}

private fun updateSelectedCyclewayFeature(
    style: Style,
    feature: Feature
) {
    val selectedSource = style.getSource(SELECTED_CYCLEWAY_SOURCE_ID) as? GeoJsonSource
        ?: return

    selectedSource.setGeoJson(
        """
        {
          "type": "FeatureCollection",
          "features": [
            ${feature.toJson()}
          ]
        }
        """.trimIndent()
    )
}

private fun clearSelectedCyclewayFeature(
    style: Style
) {
    val selectedSource = style.getSource(SELECTED_CYCLEWAY_SOURCE_ID) as? GeoJsonSource
        ?: return

    selectedSource.setGeoJson(EMPTY_FEATURE_COLLECTION)
}

private fun fitCameraToSelectedFeature(
    map: MapLibreMap,
    bottomSheetHeightPx: Int
) {
    val feature = selectedMapFeature ?: return
    val bounds = feature.getLatLngBounds() ?: return

    val horizontalPaddingPx = 96
    val topPaddingPx = 96
    val extraBottomMarginPx = 24

    map.animateCamera(
        CameraUpdateFactory.newLatLngBounds(
            bounds,
            horizontalPaddingPx,
            topPaddingPx,
            horizontalPaddingPx,
            bottomSheetHeightPx + extraBottomMarginPx
        ),
        CAMERA_ANIMATION_DURATION_MS
    )
}

private fun Feature.getLatLngBounds(): LatLngBounds? {
    val geometry = geometry() ?: return null

    val points = when (geometry) {
        is LineString -> geometry.coordinates()
        is MultiLineString -> geometry.coordinates().flatten()
        is Point -> listOf(geometry)
        else -> emptyList()
    }

    if (points.isEmpty()) return null

    val boundsBuilder = LatLngBounds.Builder()

    points.forEach { point ->
        boundsBuilder.include(
            LatLng(
                point.latitude(),
                point.longitude()
            )
        )
    }

    return boundsBuilder.build()
}

@SuppressLint("MissingPermission")
private fun enableUserLocationAndCenterOnce(
    context: Context,
    map: MapLibreMap,
    style: Style,
    onCameraCenteredOnUserLocation: () -> Unit
) {
    val locationComponent = map.locationComponent

    val options = LocationComponentOptions.builder(context)
        .pulseEnabled(true)
        .build()

    val activationOptions = LocationComponentActivationOptions
        .builder(context, style)
        .locationComponentOptions(options)
        .build()

    locationComponent.activateLocationComponent(activationOptions)
    locationComponent.isLocationComponentEnabled = true
    locationComponent.renderMode = RenderMode.COMPASS
    locationComponent.cameraMode = CameraMode.NONE

    locationComponent.lastKnownLocation?.let { location ->
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                15.0
            )
        )

        onCameraCenteredOnUserLocation()
    }
}

@SuppressLint("MissingPermission")
private fun centerCameraOnUserLocation(
    map: MapLibreMap,
    onCameraCenteredOnUserLocation: () -> Unit
) {
    val location = map.locationComponent.lastKnownLocation ?: return

    map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude),
            15.0
        )
    )

    onCameraCenteredOnUserLocation()
}

private fun Feature.toSelectedCyclewayUi(): SelectedCyclewayUi {
    return SelectedCyclewayUi(
        objectId = propertyOrFallback("OBJECTID", fallback = UNKNOWN_VALUE),
        code = propertyOrFallback("CODIGO", fallback = UNKNOWN_VALUE),
        status = formatText(propertyOrFallback("ESTADO", fallback = UNKNOWN_VALUE)),
        district = formatTitleCase(propertyOrFallback("DISTRITO", fallback = UNKNOWN_VALUE)),
        name = formatTitleCase(propertyOrFallback("NOMBRE", fallback = "Ciclovía seleccionada")),
        section = formatTitleCase(propertyOrFallback("TRAMO", fallback = UNKNOWN_VALUE)),
        roadType = formatText(propertyOrFallback("SECC_VIAL", fallback = UNKNOWN_VALUE)),
        cyclewayPosition = formatText(propertyOrFallback("UBIC_VIAL", fallback = UNKNOWN_VALUE)),
        lengthKm = formatLengthKm(propertyOrFallback("LONGITUD", fallback = UNKNOWN_VALUE)),
        direction = formatText(propertyOrFallback("DIRECC", fallback = UNKNOWN_VALUE)),
        segregationType = formatSegregationType(propertyOrFallback("SEGREGAC", fallback = UNKNOWN_VALUE)),
        lighting = formatText(propertyOrFallback("ILUMINAC", fallback = UNKNOWN_VALUE)),
        surveillance = formatText(propertyOrFallback("VIGILANCIA", fallback = UNKNOWN_VALUE)),
        projectName = formatTitleCase(propertyOrFallback("NOM_PROY", fallback = UNKNOWN_VALUE)),
        quantity = propertyOrFallback("CANTIDAD", fallback = UNKNOWN_VALUE),
        implementationType = formatText(propertyOrFallback("EMERGENTE", fallback = UNKNOWN_VALUE)),
        year = propertyOrFallback("AÑO", fallback = UNKNOWN_VALUE),
        authorityType = formatText(propertyOrFallback("ENTIDAD", fallback = UNKNOWN_VALUE)),
        creationDate = propertyOrFallback("CREACION", fallback = UNKNOWN_VALUE)
    )
}

private fun Feature.propertyOrFallback(
    vararg keys: String,
    fallback: String
): String {
    return keys.firstNotNullOfOrNull { key ->
        if (hasProperty(key) && getProperty(key) != null) {
            getProperty(key).asString.takeIf { it.isNotBlank() }
        } else {
            null
        }
    } ?: fallback
}

@SuppressLint("DefaultLocale")
private fun formatLengthKm(value: String): String {
    val numericValue = value.toDoubleOrNull()

    return if (numericValue != null && numericValue > 0.0) {
        String.format("%.2f km", numericValue)
    } else {
        "No disponible"
    }
}

private fun formatText(value: String): String {
    val normalized = value.lowercase()

    return normalized.replaceFirstChar { char ->
        char.uppercase()
    }
}

private fun formatTitleCase(value: String): String {
    val lowercaseConnectors = setOf(
        "de",
        "del",
        "la",
        "las",
        "los",
        "y",
        "e",
        "en",
        "a"
    )

    return value.lowercase()
        .split(" ")
        .filter { it.isNotBlank() }
        .mapIndexed { index, word ->
            if (index != 0 && word in lowercaseConnectors) {
                word
            } else {
                word.replaceFirstChar { char ->
                    char.uppercase()
                }
            }
        }
        .joinToString(" ")
}

private fun formatSegregationType(value: String): String {
    if (value.isBlank() || value.equals(UNKNOWN_VALUE, ignoreCase = true)) {
        return UNKNOWN_VALUE
    }

    val values = value.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { formatText(it).lowercase() }
        .distinct()

    return when (values.size) {
        0 -> UNKNOWN_VALUE
        1 -> values.first().replaceFirstChar { it.uppercase() }
        2 -> "${values[0].replaceFirstChar { it.uppercase() }} y ${values[1]}"
        else -> {
            val firstValues = values.dropLast(1)
            val lastValue = values.last()

            firstValues.joinToString(", ")
                .replaceFirstChar { it.uppercase() } + " y $lastValue"
        }
    }
}