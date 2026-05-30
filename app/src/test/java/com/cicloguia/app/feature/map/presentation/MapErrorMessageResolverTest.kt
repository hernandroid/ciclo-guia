package com.cicloguia.app.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class MapErrorMessageResolverTest {

    @Test
    fun `noAvailableDataMessage returns connection message for io errors`() {
        val result = MapErrorMessageResolver.noAvailableDataMessage(
            IOException("Unable to resolve host")
        )

        assertEquals(
            "No pudimos cargar las ciclovías. Revisa tu conexión e inténtalo de nuevo.",
            result
        )
    }

    @Test
    fun `noAvailableDataMessage returns dataset message for invalid geojson errors`() {
        val result = MapErrorMessageResolver.noAvailableDataMessage(
            IllegalStateException("Downloaded cycleways GeoJSON is invalid")
        )

        assertEquals(
            "La información de ciclovías no está disponible por el momento. Inténtalo nuevamente más tarde.",
            result
        )
    }

    @Test
    fun `noAvailableDataMessage returns dataset message for checksum errors`() {
        val result = MapErrorMessageResolver.noAvailableDataMessage(
            IllegalStateException("Downloaded cycleways GeoJSON checksum mismatch")
        )

        assertEquals(
            "La información de ciclovías no está disponible por el momento. Inténtalo nuevamente más tarde.",
            result
        )
    }

    @Test
    fun `noAvailableDataMessage returns generic message for unexpected errors`() {
        val result = MapErrorMessageResolver.noAvailableDataMessage(
            RuntimeException("Unexpected failure")
        )

        assertEquals(
            "No pudimos cargar el mapa de ciclovías. Inténtalo nuevamente.",
            result
        )
    }

    @Test
    fun `synchronizationFailedWithAvailableDataMessage returns friendly cached data message`() {
        val result = MapErrorMessageResolver.synchronizationFailedWithAvailableDataMessage()

        assertEquals(
            "No pudimos actualizar el mapa. Estás viendo los datos disponibles en este momento.",
            result
        )
    }
}
