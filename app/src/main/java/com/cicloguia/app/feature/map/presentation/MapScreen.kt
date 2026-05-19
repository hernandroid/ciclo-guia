package com.cicloguia.app.feature.map.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.cicloguia.app.BuildConfig
import com.cicloguia.app.feature.map.presentation.components.CyclewayDetailSheet
import com.cicloguia.app.feature.map.presentation.components.CyclewaysMapView
import com.cicloguia.app.feature.map.presentation.components.MapLegendCard
import com.cicloguia.app.feature.map.presentation.components.MapLoadingOverlay

@Composable
fun MapScreen(
    uiState: MapUiState,
    hasLocationPermission: Boolean,
    onEvent: (MapUiEvent) -> Unit
) {
    var isLegendExpanded by remember {
        mutableStateOf(false)
    }

    var bottomSheetHeightPx by remember {
        mutableIntStateOf(0)
    }

    var selectedCyclewayCameraFitRequest by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        floatingActionButton = {
            if (uiState is MapUiState.Content) {
                SmallFloatingActionButton(
                    modifier = Modifier.padding(
                        end = 12.dp,
                        bottom = 18.dp
                    ),
                    onClick = {
                        onEvent(MapUiEvent.CenterOnUserLocationClicked)
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = if (uiState.isFollowingUserLocation) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
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
                        selectedCycleway = uiState.selectedCycleway,
                        selectedCyclewayCameraFitRequest = selectedCyclewayCameraFitRequest,
                        bottomSheetHeightPx = bottomSheetHeightPx,
                        onCameraCenteredOnUserLocation = {
                            onEvent(MapUiEvent.CameraCenteredOnUserLocation)
                        },
                        onMapMovedByUser = {
                            onEvent(MapUiEvent.MapMovedByUser)
                        },
                        onCyclewayClick = { cycleway ->
                            bottomSheetHeightPx = 0
                            onEvent(MapUiEvent.CyclewayClicked(cycleway))
                        }
                    )

                    MapLegendCard(
                        legend = uiState.legend,
                        expanded = isLegendExpanded,
                        onClick = {
                            isLegendExpanded = !isLegendExpanded
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 24.dp)
                    )

                    if (uiState.isSyncing) {
                        MapLoadingOverlay(
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                    uiState.selectedCycleway?.let { cycleway ->
                        CyclewayDetailSheet(
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                val newHeight = coordinates.size.height

                                if (newHeight > 0 && newHeight != bottomSheetHeightPx) {
                                    bottomSheetHeightPx = newHeight
                                    selectedCyclewayCameraFitRequest++
                                }
                            },
                            cycleway = cycleway,
                            onDismiss = {
                                bottomSheetHeightPx = 0
                                onEvent(MapUiEvent.DismissSelectedCycleway)
                            },
                            onViewRouteClick = {
                                // TODO: Implement route action
                            }
                        )
                    }
                }
            }
        }
    }
}