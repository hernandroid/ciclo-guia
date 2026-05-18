package com.cicloguia.app.feature.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cicloguia.app.feature.map.presentation.components.CyclewayDetailSheet
import com.cicloguia.app.feature.map.presentation.components.CyclewaysMapView
import com.cicloguia.app.feature.map.presentation.components.MapLoadingOverlay

@Composable
fun MapScreen(
    uiState: MapUiState,
    hasLocationPermission: Boolean,
    onEvent: (MapUiEvent) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (uiState is MapUiState.Content) {
                SmallFloatingActionButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        onEvent(MapUiEvent.CenterOnUserLocationClicked)
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centrar ubicación"
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
            when (uiState) {
                MapUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is MapUiState.Error -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = {
                            onEvent(MapUiEvent.RetryClicked)
                        }
                    ) {
                        Text(text = "Reintentar")
                    }
                }

                is MapUiState.Content -> {
                    CyclewaysMapView(
                        styleUrl = uiState.mapStyleUrl,
                        geoJson = uiState.geoJson,
                        hasLocationPermission = hasLocationPermission,
                        centerOnUserLocationRequest = uiState.centerOnUserLocationRequest,
                        onCyclewayClick = { cycleway ->
                            onEvent(MapUiEvent.CyclewayClicked(cycleway))
                        }
                    )

                    if (uiState.isSyncing) {
                        MapLoadingOverlay(
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                    uiState.selectedCycleway?.let { cycleway ->
                        CyclewayDetailSheet(
                            cycleway = cycleway,
                            onDismiss = {
                                onEvent(MapUiEvent.DismissSelectedCycleway)
                            },
                            onViewRouteClick = {

                            }
                        )
                    }
                }
            }
        }
    }
}