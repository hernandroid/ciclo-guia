package com.cicloguia.app.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapDataSourceMessageResolverTest {

    @Test
    fun `indicatorMessage returns null for updated non syncing data`() {
        val result = MapDataSourceMessageResolver.indicatorMessage(
            dataSource = MapDataSourceUi.Updated,
            isSyncing = false
        )

        assertNull(result)
    }

    @Test
    fun `indicatorMessage returns updating message for updated syncing data`() {
        val result = MapDataSourceMessageResolver.indicatorMessage(
            dataSource = MapDataSourceUi.Updated,
            isSyncing = true
        )

        assertEquals("Actualizando ciclovías...", result)
    }

    @Test
    fun `indicatorMessage returns cached data message`() {
        val result = MapDataSourceMessageResolver.indicatorMessage(
            dataSource = MapDataSourceUi.DownloadedCache,
            isSyncing = false
        )

        assertEquals("Mostrando datos guardados.", result)
    }

    @Test
    fun `indicatorMessage returns embedded offline data message`() {
        val result = MapDataSourceMessageResolver.indicatorMessage(
            dataSource = MapDataSourceUi.EmbeddedAsset,
            isSyncing = false
        )

        assertEquals("Mostrando datos incluidos sin conexión.", result)
    }
}
