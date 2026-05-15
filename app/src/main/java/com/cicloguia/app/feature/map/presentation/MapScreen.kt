package com.cicloguia.app.feature.map.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            ExtendedFloatingActionButton(
                onClick = {
                    onEvent(MapUiEvent.ReportClicked)
                },
                elevation = FloatingActionButtonDefaults.elevation()
            ) {
                Text(text = "Reportar")
            }
        },
        bottomBar = {
            if (uiState is MapUiState.Content) {
                BottomAppBar {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = uiState.selectedCyclewayName,
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
                        hasLocationPermission = hasLocationPermission
                    )

                    if (uiState.isSyncing) {
                        MapLoadingOverlay(
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}