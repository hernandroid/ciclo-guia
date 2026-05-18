package com.cicloguia.app.feature.map.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
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
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature

private const val CYCLEWAYS_SOURCE_ID = "cicloguia-cycleways-source"
private const val CYCLEWAYS_LAYER_ID = "cicloguia-cycleways-layer"
private const val UNKNOWN_VALUE = "No especificado"
private const val CYCLEWAY_TAP_TOLERANCE_PX = 32.0

@Composable
fun CyclewaysMapView(
    styleUrl: String,
    geoJson: String,
    hasLocationPermission: Boolean,
    centerOnUserLocationRequest: Int,
    onCyclewayClick: (SelectedCyclewayUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(modifier = modifier.fillMaxSize(), factory = {
        mapView.apply {
            getMapAsync { map ->
                setupMap(
                    context = context,
                    map = map,
                    styleUrl = styleUrl,
                    geoJson = geoJson,
                    hasLocationPermission = hasLocationPermission,
                    onCyclewayClick = onCyclewayClick
                )
            }
        }
    }, update = {
        it.getMapAsync { map ->
            updateCyclewaysLayer(
                map = map, geoJson = geoJson
            )

            if (hasLocationPermission && centerOnUserLocationRequest > 0) {
                centerCameraOnUserLocation(map)
            }
        }
    })
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
    onCyclewayClick: (SelectedCyclewayUi) -> Unit
) {
    map.cameraPosition =
        CameraPosition.Builder().target(LatLng(-12.0945, -77.0445)).zoom(12.0).build()

    map.setStyle(
        Style.Builder().fromUri(styleUrl)
    ) { style ->
        addOrUpdateCyclewaysLayer(
            style = style, geoJson = geoJson
        )

        setupCyclewayClickListener(
            map = map, onCyclewayClick = onCyclewayClick
        )

        if (hasLocationPermission) {
            enableUserLocationAndCenterOnce(
                context = context, map = map, style = style
            )
        }
    }
}

private fun updateCyclewaysLayer(
    map: MapLibreMap, geoJson: String
) {
    map.style?.let { style ->
        addOrUpdateCyclewaysLayer(
            style = style, geoJson = geoJson
        )
    }
}

private fun addOrUpdateCyclewaysLayer(
    style: Style, geoJson: String
) {
    val existingSource = style.getSource(CYCLEWAYS_SOURCE_ID) as? GeoJsonSource

    if (existingSource != null) {
        existingSource.setGeoJson(geoJson)
    } else {
        style.addSource(
            GeoJsonSource(
                CYCLEWAYS_SOURCE_ID, geoJson
            )
        )
    }

    if (style.getLayer(CYCLEWAYS_LAYER_ID) == null) {
        style.addLayer(
            LineLayer(CYCLEWAYS_LAYER_ID, CYCLEWAYS_SOURCE_ID).withProperties(
                lineColor("#00B37A"),
                lineWidth(2.5f),
                lineOpacity(0.95f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
            )
        )
    }
}

private fun setupCyclewayClickListener(
    map: MapLibreMap,
    onCyclewayClick: (SelectedCyclewayUi) -> Unit
) {
    map.addOnMapClickListener { latLng ->
        val screenPoint = map.projection.toScreenLocation(latLng)

        val tapBox = android.graphics.RectF(
            (screenPoint.x - CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.y - CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.x + CYCLEWAY_TAP_TOLERANCE_PX).toFloat(),
            (screenPoint.y + CYCLEWAY_TAP_TOLERANCE_PX).toFloat()
        )

        val features = map.queryRenderedFeatures(
            tapBox,
            CYCLEWAYS_LAYER_ID
        ).toList()

        val selectedFeature = features.firstOrNull()

        if (selectedFeature != null) {
            onCyclewayClick(selectedFeature.toSelectedCyclewayUi())
            true
        } else {
            false
        }
    }
}

@SuppressLint("MissingPermission")
private fun enableUserLocationAndCenterOnce(
    context: Context, map: MapLibreMap, style: Style
) {
    val locationComponent = map.locationComponent

    val options = LocationComponentOptions.builder(context).pulseEnabled(true).build()

    val activationOptions =
        LocationComponentActivationOptions.builder(context, style).locationComponentOptions(options)
            .build()

    locationComponent.activateLocationComponent(activationOptions)
    locationComponent.isLocationComponentEnabled = true
    locationComponent.renderMode = RenderMode.COMPASS

    locationComponent.lastKnownLocation?.let { location ->
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude), 15.0
            )
        )
    }

    locationComponent.cameraMode = CameraMode.NONE
}

@SuppressLint("MissingPermission")
private fun centerCameraOnUserLocation(
    map: MapLibreMap
) {
    val location = map.locationComponent.lastKnownLocation ?: return

    map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude), 15.0
        )
    )
}

private fun Feature.toSelectedCyclewayUi(): SelectedCyclewayUi {
    return SelectedCyclewayUi(
        objectId = propertyOrFallback(
            "OBJECTID", fallback = UNKNOWN_VALUE
        ),
        code = propertyOrFallback(
            "CODIGO", fallback = UNKNOWN_VALUE
        ),
        status = formatText(
            propertyOrFallback(
                "ESTADO", fallback = UNKNOWN_VALUE
            )
        ),
        district = formatTitleCase(
            propertyOrFallback(
                "DISTRITO", fallback = UNKNOWN_VALUE
            )
        ),
        name = formatTitleCase(
            propertyOrFallback(
                "NOMBRE", fallback = "Ciclovía seleccionada"
            )
        ),
        section = formatTitleCase(
            propertyOrFallback(
                "TRAMO", fallback = UNKNOWN_VALUE
            )
        ),
        roadType = formatText(
            propertyOrFallback(
                "SECC_VIAL", fallback = UNKNOWN_VALUE
            )
        ),
        cyclewayPosition = formatText(
            propertyOrFallback(
                "UBIC_VIAL", fallback = UNKNOWN_VALUE
            )
        ),
        lengthKm = formatLengthKm(
            propertyOrFallback(
                "LONGITUD", fallback = UNKNOWN_VALUE
            )
        ),
        direction = formatText(
            propertyOrFallback(
                "DIRECC", fallback = UNKNOWN_VALUE
            )
        ),
        segregationType = formatSegregationType(
            propertyOrFallback(
                "SEGREGAC", fallback = UNKNOWN_VALUE
            )
        ),
        lighting = formatText(
            propertyOrFallback(
                "ILUMINAC", fallback = UNKNOWN_VALUE
            )
        ),
        surveillance = formatText(
            propertyOrFallback(
                "VIGILANCIA", fallback = UNKNOWN_VALUE
            )
        ),
        projectName = formatTitleCase(
            propertyOrFallback(
                "NOM_PROY", fallback = UNKNOWN_VALUE
            )
        ),
        quantity = propertyOrFallback(
            "CANTIDAD", fallback = UNKNOWN_VALUE
        ),
        implementationType = formatText(
            propertyOrFallback(
                "EMERGENTE", fallback = UNKNOWN_VALUE
            )
        ),
        year = propertyOrFallback(
            "AÑO", fallback = UNKNOWN_VALUE
        ),
        authorityType = formatText(
            propertyOrFallback(
                "ENTIDAD", fallback = UNKNOWN_VALUE
            )
        ),
        creationDate = propertyOrFallback(
            "CREACION", fallback = UNKNOWN_VALUE
        )
    )
}

private fun Feature.propertyOrFallback(
    vararg keys: String, fallback: String
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
        "de", "del", "la", "las", "los", "y", "e", "en", "a"
    )

    return value.lowercase().split(" ").filter { it.isNotBlank() }.mapIndexed { index, word ->
        if (index != 0 && word in lowercaseConnectors) {
            word
        } else {
            word.replaceFirstChar { char ->
                char.uppercase()
            }
        }
    }.joinToString(" ")
}

private fun formatSegregationType(value: String): String {
    if (
        value.isBlank() ||
        value.equals(UNKNOWN_VALUE, ignoreCase = true)
    ) {
        return UNKNOWN_VALUE
    }

    val values = value
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { formatText(it).lowercase() }
        .distinct()

    return when (values.size) {
        0 -> UNKNOWN_VALUE

        1 -> {
            values.first()
                .replaceFirstChar { it.uppercase() }
        }

        2 -> {
            "${values[0].replaceFirstChar { it.uppercase() }} y ${values[1]}"
        }

        else -> {
            val firstValues = values.dropLast(1)
            val lastValue = values.last()

            firstValues.joinToString(", ")
                .replaceFirstChar { it.uppercase() } +
                    " y $lastValue"
        }
    }
}