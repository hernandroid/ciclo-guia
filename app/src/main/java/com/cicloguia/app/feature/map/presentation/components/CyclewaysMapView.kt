package com.cicloguia.app.feature.map.presentation.components

import android.annotation.SuppressLint
import android.content.Context
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
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource

@Composable
fun CyclewaysMapView(
    styleUrl: String,
    geoJson: String,
    hasLocationPermission: Boolean,
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
                        hasLocationPermission = hasLocationPermission
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
    hasLocationPermission: Boolean
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
    val sourceId = "cicloguia-cycleways-source"
    val layerId = "cicloguia-cycleways-layer"

    val existingSource = style.getSource(sourceId) as? GeoJsonSource

    if (existingSource != null) {
        existingSource.setGeoJson(geoJson)
    } else {
        style.addSource(
            GeoJsonSource(
                sourceId,
                geoJson
            )
        )
    }

    if (style.getLayer(layerId) == null) {
        style.addLayer(
            LineLayer(layerId, sourceId).withProperties(
                lineColor("#00E676"),
                lineWidth(5f)
            )
        )
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