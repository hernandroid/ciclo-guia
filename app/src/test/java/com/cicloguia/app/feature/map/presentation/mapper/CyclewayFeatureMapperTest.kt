package com.cicloguia.app.feature.map.presentation.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

class CyclewayFeatureMapperTest {

    @Test
    fun `toSelectedCyclewayUi uses middle line coordinate as destination`() {
        val feature = Feature.fromGeometry(
            LineString.fromLngLats(
                listOf(
                    Point.fromLngLat(-77.01, -12.01),
                    Point.fromLngLat(-77.02, -12.02),
                    Point.fromLngLat(-77.03, -12.03)
                )
            )
        )

        val result = feature.toSelectedCyclewayUi()

        assertEquals(-12.02, result.destination?.latitude ?: 0.0, DELTA)
        assertEquals(-77.02, result.destination?.longitude ?: 0.0, DELTA)
    }

    @Test
    fun `toSelectedCyclewayUi uses point geometry as destination`() {
        val feature = Feature.fromGeometry(
            Point.fromLngLat(-77.04, -12.04)
        )

        val result = feature.toSelectedCyclewayUi()

        assertEquals(-12.04, result.destination?.latitude ?: 0.0, DELTA)
        assertEquals(-77.04, result.destination?.longitude ?: 0.0, DELTA)
    }

    @Test
    fun `toSelectedCyclewayUi leaves destination empty when geometry is missing`() {
        val feature = Feature.fromJson("""{"type":"Feature","properties":{}}""")

        val result = feature.toSelectedCyclewayUi()

        assertNull(result.destination)
    }

    private companion object {
        const val DELTA = 0.000001
    }
}
