package com.cicloguia.app.feature.map.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.MultiLineString
import org.maplibre.geojson.Point

internal fun fitCameraToFeature(
    map: MapLibreMap,
    feature: Feature,
    bottomSheetHeightPx: Int
) {
    val bounds = feature.getLatLngBounds() ?: return

    map.animateCamera(
        CameraUpdateFactory.newLatLngBounds(
            bounds,
            CAMERA_HORIZONTAL_PADDING_PX,
            CAMERA_TOP_PADDING_PX,
            CAMERA_HORIZONTAL_PADDING_PX,
            bottomSheetHeightPx + CAMERA_EXTRA_BOTTOM_MARGIN_PX
        ),
        CAMERA_ANIMATION_DURATION_MS
    )
}

@SuppressLint("MissingPermission")
internal fun enableUserLocationAndCenterOnce(
    context: Context,
    map: MapLibreMap,
    style: Style,
    hasLocationPermission: Boolean,
    onCameraCenteredOnUserLocation: () -> Unit
) {
    if (!hasLocationPermission) return

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
                USER_LOCATION_ZOOM
            )
        )

        onCameraCenteredOnUserLocation()
    }
}

@SuppressLint("MissingPermission")
internal fun centerCameraOnUserLocation(
    map: MapLibreMap,
    hasLocationPermission: Boolean,
    onCameraCenteredOnUserLocation: () -> Unit
) {
    if (!hasLocationPermission) return

    val location = map.locationComponent.lastKnownLocation ?: return

    map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude),
            USER_LOCATION_ZOOM
        )
    )

    onCameraCenteredOnUserLocation()
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

    return LatLngBounds.Builder().apply {
        points.forEach { point ->
            include(
                LatLng(
                    point.latitude(),
                    point.longitude()
                )
            )
        }
    }.build()
}

private const val CAMERA_HORIZONTAL_PADDING_PX = 96
private const val CAMERA_TOP_PADDING_PX = 96
private const val CAMERA_EXTRA_BOTTOM_MARGIN_PX = 160
private const val CAMERA_ANIMATION_DURATION_MS = 900
private const val USER_LOCATION_ZOOM = 15.0