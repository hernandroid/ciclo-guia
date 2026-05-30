package com.cicloguia.app.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class MapLocationPermissionStatusTest {

    @Test
    fun `resolveMapLocationPermissionStatus returns granted when permission is granted`() {
        val result = resolveMapLocationPermissionStatus(
            granted = true,
            shouldShowRationale = false,
            hasRequestedPermissionFromMap = false
        )

        assertEquals(MapLocationPermissionStatus.Granted, result)
    }

    @Test
    fun `resolveMapLocationPermissionStatus returns denied before map request`() {
        val result = resolveMapLocationPermissionStatus(
            granted = false,
            shouldShowRationale = false,
            hasRequestedPermissionFromMap = false
        )

        assertEquals(MapLocationPermissionStatus.Denied, result)
    }

    @Test
    fun `resolveMapLocationPermissionStatus returns denied when rationale should be shown`() {
        val result = resolveMapLocationPermissionStatus(
            granted = false,
            shouldShowRationale = true,
            hasRequestedPermissionFromMap = true
        )

        assertEquals(MapLocationPermissionStatus.Denied, result)
    }

    @Test
    fun `resolveMapLocationPermissionStatus returns permanently denied after map request without rationale`() {
        val result = resolveMapLocationPermissionStatus(
            granted = false,
            shouldShowRationale = false,
            hasRequestedPermissionFromMap = true
        )

        assertEquals(MapLocationPermissionStatus.PermanentlyDenied, result)
    }
}
