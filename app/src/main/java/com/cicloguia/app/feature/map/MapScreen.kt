package com.cicloguia.app.feature.map

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cicloguia.app.core.map.MapStyleProvider
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource

@Composable
fun MapScreen(
    mapStyleProvider: MapStyleProvider,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.onReportClicked()
                    Toast.makeText(
                        context,
                        "Próximamente: reportar ciclovía o incidente",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                elevation = FloatingActionButtonDefaults.elevation()
            ) {
                Text(text = "Reportar")
            }
        },
        bottomBar = {
            val state = uiState

            if (state is MapUiState.Success) {
                BottomAppBar {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = state.selectedCyclewayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                MapUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is MapUiState.Error -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is MapUiState.Success -> {
                    CyclewaysMapView(
                        mapView = mapView,
                        styleUrl = mapStyleProvider.getStyleUrl(),
                        geoJson = state.geoJson
                    )

                    if (state.isSyncing) {
                        LinearProgressIndicator(
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CyclewaysMapView(
    mapView: MapView,
    styleUrl: String,
    geoJson: String
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    setupMap(
                        map = map,
                        styleUrl = styleUrl,
                        geoJson = geoJson
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
    map: MapLibreMap,
    styleUrl: String,
    geoJson: String
) {
    map.cameraPosition = CameraPosition.Builder()
        .target(LatLng(-12.0945, -77.0445))
        .zoom(12.0)
        .build()

    map.setStyle(
        Style.Builder().fromUri(styleUrl)
    ) {
        addOrUpdateCyclewaysLayer(
            style = it,
            geoJson = geoJson
        )
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
                lineColor("#0066FF"),
                lineWidth(5f)
            )
        )
    }
}