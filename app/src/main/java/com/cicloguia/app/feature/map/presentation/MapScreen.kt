package com.cicloguia.app.feature.map.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import com.cicloguia.app.feature.map.presentation.components.CyclewayDetailSheet
import com.cicloguia.app.feature.map.presentation.components.CyclewaysMapView
import com.cicloguia.app.feature.map.presentation.components.MapLegendCard
import com.cicloguia.app.feature.map.presentation.components.MapLoadingOverlay
import com.cicloguia.app.feature.map.presentation.model.CyclewayLegendUi

@Composable
fun MapScreen(
    uiState: MapUiState,
    locationPermissionStatus: MapLocationPermissionStatus,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
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

    var showLocationPermissionDialog by remember {
        mutableStateOf(false)
    }

    val hasLocationPermission =
        locationPermissionStatus == MapLocationPermissionStatus.Granted

    Scaffold(
        floatingActionButton = {
            if (uiState is MapUiState.Content) {
                SmallFloatingActionButton(
                    modifier = Modifier.padding(
                        end = 12.dp,
                        bottom = 18.dp
                    ),
                    onClick = {
                        if (hasLocationPermission) {
                            onEvent(MapUiEvent.CenterOnUserLocationClicked)
                        } else {
                            showLocationPermissionDialog = true
                        }
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

                    MapDataSourceIndicator(
                        dataSource = uiState.dataSource,
                        isSyncing = uiState.isSyncing,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )

                    if (uiState.legend.isEmpty()) {
                        EmptyCyclewaysOverlay(
                            modifier = Modifier.align(Alignment.Center)
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
                                onEvent(MapUiEvent.StartRouteClicked)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showLocationPermissionDialog) {
        LocationPermissionDialog(
            permissionStatus = locationPermissionStatus,
            onDismiss = {
                showLocationPermissionDialog = false
            },
            onRequestLocationPermission = {
                showLocationPermissionDialog = false
                onRequestLocationPermission()
            },
            onOpenLocationSettings = {
                showLocationPermissionDialog = false
                onOpenLocationSettings()
            }
        )
    }
}

@Composable
private fun LocationPermissionDialog(
    permissionStatus: MapLocationPermissionStatus,
    onDismiss: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    val isPermanentlyDenied =
        permissionStatus == MapLocationPermissionStatus.PermanentlyDenied

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Activa tu ubicación")
        },
        text = {
            Text(
                text = if (isPermanentlyDenied) {
                    "El permiso de ubicación está desactivado para Ciclo Guía. Actívalo desde los ajustes de Android para centrar el mapa en tu posición."
                } else {
                    "Tu ubicación nos ayuda a centrar el mapa en tu posición actual. Puedes seguir usando Ciclo Guía aunque no actives este permiso."
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = if (isPermanentlyDenied) {
                    onOpenLocationSettings
                } else {
                    onRequestLocationPermission
                }
            ) {
                Text(
                    text = if (isPermanentlyDenied) {
                        "Abrir ajustes"
                    } else {
                        "Permitir ubicación"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Ahora no")
            }
        }
    )
}

@Composable
private fun MapDataSourceIndicator(
    dataSource: MapDataSourceUi,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    val message = MapDataSourceMessageResolver.indicatorMessage(
        dataSource = dataSource,
        isSyncing = isSyncing
    ) ?: return

    Surface(
        modifier = modifier.widthIn(max = 340.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyCyclewaysOverlay(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 360.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "No hay ciclovías para mostrar",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = "El mapa está disponible, pero no encontramos ciclovías en los datos cargados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun CyclewayLegendUi.isEmpty(): Boolean {
    return existingCount == 0 &&
        plannedCount == 0 &&
        underConstructionCount == 0
}
