package com.cicloguia.app.feature.map.presentation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cicloguia.app.feature.map.presentation.model.CyclewayDestinationUi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapRoute(
    onNavigateToReport: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasRequestedLocationPermissionFromMap by rememberSaveable {
        mutableStateOf(false)
    }

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    val locationPermissionStatus = resolveMapLocationPermissionStatus(
        granted = locationPermissionState.status.isGranted,
        shouldShowRationale = locationPermissionState.status.shouldShowRationale,
        hasRequestedPermissionFromMap = hasRequestedLocationPermissionFromMap
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MapUiEffect.NavigateToReport -> onNavigateToReport()
                is MapUiEffect.OpenExternalNavigation -> {
                    val opened = openExternalNavigation(
                        context = context,
                        destination = effect.destination
                    )

                    if (!opened) {
                        showMessage(
                            context = context,
                            message = "No se encontró una aplicación de mapas compatible."
                        )
                    }
                }

                is MapUiEffect.ShowMessage -> {
                    showMessage(
                        context = context,
                        message = effect.message
                    )
                }
            }
        }
    }

    MapScreen(
        uiState = uiState,
        locationPermissionStatus = locationPermissionStatus,
        onRequestLocationPermission = {
            hasRequestedLocationPermissionFromMap = true
            locationPermissionState.launchPermissionRequest()
        },
        onOpenLocationSettings = {
            context.openApplicationSettings()
        },
        onEvent = viewModel::onEvent
    )
}

private fun openExternalNavigation(
    context: Context,
    destination: CyclewayDestinationUi
): Boolean {
    val googleMapsIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(
            "google.navigation:q=${destination.latitude},${destination.longitude}&mode=b"
        )
    ).setPackage(GOOGLE_MAPS_PACKAGE)

    if (googleMapsIntent.canBeHandled(context)) {
        return context.startActivitySafely(googleMapsIntent)
    }

    val genericMapsIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(
            "geo:${destination.latitude},${destination.longitude}" +
                "?q=${destination.latitude},${destination.longitude}"
        )
    )

    if (genericMapsIntent.canBeHandled(context)) {
        return context.startActivitySafely(genericMapsIntent)
    }

    return false
}

private fun Intent.canBeHandled(context: Context): Boolean {
    return resolveActivity(context.packageManager) != null
}

private fun Context.startActivitySafely(intent: Intent): Boolean {
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

private fun showMessage(
    context: Context,
    message: String
) {
    Toast
        .makeText(context, message, Toast.LENGTH_SHORT)
        .show()
}

private fun Context.openApplicationSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )

    startActivitySafely(intent)
}

private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
