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
                        onCyclewayClick = onCyclewayClick
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

                if (hasLocationPermission && centerOnUserLocationRequest > 0) {
                    centerCameraOnUserLocation(map)
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
    onCyclewayClick: (SelectedCyclewayUi) -> Unit
) {
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

        if (hasLocationPermission) {
            enableUserLocationAndCenterOnce(
                context = context,
                map = map,
                style = style
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

        val features = map.queryRenderedFeatures(
            screenPoint,
            CYCLEWAYS_LAYER_ID
        )

        val selectedFeature = features.firstOrNull()

        if (selectedFeature != null) {
            onCyclewayClick(
                selectedFeature.toSelectedCyclewayUi()
            )
            true
        } else {
            false
        }
    }
}

@SuppressLint("MissingPermission")
private fun enableUserLocationAndCenterOnce(
    context: Context,
    map: MapLibreMap,
    style: Style
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

    locationComponent.lastKnownLocation?.let { location ->
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                15.0
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
            LatLng(location.latitude, location.longitude),
            15.0
        )
    )
}

private fun Feature.toSelectedCyclewayUi(): SelectedCyclewayUi {
    return SelectedCyclewayUi(
        name = propertyOrFallback(
            "NOMBRE",
            "NOMBRE_VIA",
            "name",
            "CODIGO",
            fallback = "Ciclovía seleccionada"
        ),
        district = formatTitleCase(
            propertyOrFallback(
                "DISTRITO",
                "distrito",
                fallback = "Distrito no especificado"
            )
        ),
        lengthKm = formatLengthKm(
            propertyOrFallback(
                "LONG_KM",
                "LONGITUD",
                "longitud",
                fallback = "No disponible"
            )
        ),
        segregationType = formatSegregationType(
            propertyOrFallback(
                "TIPO_SEG",
                "SEGREGACION",
                "SEGREGAC",
                "tipo_seg",
                fallback = "No especificado"
            )
        ),
        status = formatText(
            propertyOrFallback(
                "ESTADO",
                "estado",
                fallback = "No especificado"
            )
        ),
        direction = formatText(
            propertyOrFallback(
                "SENTIDO",
                "sentido",
                "DIRECC",
                fallback = "No especificado"
            )
        ),
        lighting = formatText(
            propertyOrFallback(
                "ILUMINACION",
                "ILUMINAC",
                "iluminacion",
                fallback = "No especificado"
            )
        ),
        surveillance = formatText(
            propertyOrFallback(
                "VIGILANCIA",
                "VIGILANC",
                "vigilancia",
                fallback = "No especificado"
            )
        )
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
        "de", "del", "la", "las", "los", "y", "e", "en", "a"
    )

    return value
        .lowercase()
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
    return when (value.trim().lowercase()) {
        "bolardo, tachon",
        "bolardo, tachón" -> "Bolardos y tachones"

        "pintura" -> "Pintura"
        "sardinel" -> "Sardinel"
        "tachon", "tachón" -> "Tachones"
        "bolardo" -> "Bolardos"
        "no especificado" -> "No especificado"

        else -> formatText(value)
    }
}