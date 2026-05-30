package com.cicloguia.app.feature.map.presentation

enum class MapLocationPermissionStatus {
    Granted,
    Denied,
    PermanentlyDenied
}

fun resolveMapLocationPermissionStatus(
    granted: Boolean,
    shouldShowRationale: Boolean,
    hasRequestedPermissionFromMap: Boolean
): MapLocationPermissionStatus {
    return when {
        granted -> MapLocationPermissionStatus.Granted
        !shouldShowRationale && hasRequestedPermissionFromMap ->
            MapLocationPermissionStatus.PermanentlyDenied

        else -> MapLocationPermissionStatus.Denied
    }
}
