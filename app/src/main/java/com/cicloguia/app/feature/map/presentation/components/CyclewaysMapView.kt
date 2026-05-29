package com.cicloguia.app.feature.map.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cicloguia.app.feature.map.presentation.mapper.toSelectedCyclewayUi
import com.cicloguia.app.feature.map.presentation.model.SelectedCyclewayUi
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature

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
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()

    val mapState = remember {
        mutableStateOf<MapLibreMap?>(null)
    }

    val highlightedFeature = remember {
        mutableStateOf<Feature?>(null)
    }

    val lastHandledCameraFitRequest = remember {
        mutableIntStateOf(0)
    }

    val listenersRegistered = remember {
        mutableStateOf(false)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    mapState.value = map

                    map.uiSettings.isLogoEnabled = false
                    map.uiSettings.isAttributionEnabled = false

                    map.cameraPosition = CameraPosition.Builder()
                        .target(
                            LatLng(
                                DEFAULT_CAMERA_LATITUDE,
                                DEFAULT_CAMERA_LONGITUDE
                            )
                        )
                        .zoom(DEFAULT_CAMERA_ZOOM)
                        .build()

                    map.setStyle(
                        Style.Builder().fromUri(styleUrl)
                    ) { style ->
                        addOrUpdateCyclewayLayers(
                            style = style,
                            geoJson = geoJson
                        )

                        if (!listenersRegistered.value) {
                            map.registerCyclewayClickListener(
                                onFeatureSelected = { feature ->
                                    highlightedFeature.value = feature
                                    lastHandledCameraFitRequest.intValue = 0

                                    map.style?.updateHighlightedCyclewayFeature(feature)

                                    onCyclewayClick(feature.toSelectedCyclewayUi())
                                },
                                onEmptyAreaClicked = {
                                    highlightedFeature.value = null
                                    lastHandledCameraFitRequest.intValue = 0

                                    map.style?.clearHighlightedCyclewayFeature()
                                }
                            )

                            map.addOnCameraMoveStartedListener { reason ->
                                if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                                    onMapMovedByUser()
                                }
                            }

                            listenersRegistered.value = true
                        }

                        if (hasLocationPermission) {
                            enableUserLocationAndCenterOnce(
                                context = context,
                                map = map,
                                style = style,
                                hasLocationPermission = hasLocationPermission,
                                onCameraCenteredOnUserLocation = onCameraCenteredOnUserLocation
                            )
                        }
                    }
                }
            }
        },
        update = {}
    )

    LaunchedEffect(geoJson) {
        mapState.value?.let { map ->
            updateCyclewayLayers(
                map = map,
                geoJson = geoJson
            )
        }
    }

    LaunchedEffect(selectedCycleway) {
        if (selectedCycleway == null) {
            highlightedFeature.value = null
            lastHandledCameraFitRequest.intValue = 0
            mapState.value?.style?.clearHighlightedCyclewayFeature()
        }
    }

    LaunchedEffect(
        selectedCyclewayCameraFitRequest,
        bottomSheetHeightPx,
        highlightedFeature.value
    ) {
        val map = mapState.value ?: return@LaunchedEffect
        val feature = highlightedFeature.value ?: return@LaunchedEffect

        if (
            selectedCyclewayCameraFitRequest > 0 &&
            selectedCyclewayCameraFitRequest != lastHandledCameraFitRequest.intValue &&
            bottomSheetHeightPx > 0
        ) {
            fitCameraToFeature(
                map = map,
                feature = feature,
                bottomSheetHeightPx = bottomSheetHeightPx
            )

            lastHandledCameraFitRequest.intValue = selectedCyclewayCameraFitRequest
        }
    }

    LaunchedEffect(
        centerOnUserLocationRequest,
        hasLocationPermission
    ) {
        val map = mapState.value ?: return@LaunchedEffect

        if (hasLocationPermission && centerOnUserLocationRequest > 0) {
            centerCameraOnUserLocation(
                map = map,
                hasLocationPermission = hasLocationPermission,
                onCameraCenteredOnUserLocation = onCameraCenteredOnUserLocation
            )
        }
    }
}

private const val DEFAULT_CAMERA_LATITUDE = -12.0945
private const val DEFAULT_CAMERA_LONGITUDE = -77.0445
private const val DEFAULT_CAMERA_ZOOM = 12.0