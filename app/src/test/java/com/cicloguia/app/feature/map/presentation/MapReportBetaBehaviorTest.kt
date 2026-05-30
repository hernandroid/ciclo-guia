package com.cicloguia.app.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapReportBetaBehaviorTest {

    @Test
    fun `reportClickedEffect returns upcoming message in Spanish`() {
        val effect = MapReportBetaBehavior.reportClickedEffect()

        assertTrue(effect is MapUiEffect.ShowMessage)
        assertEquals(
            "La función para reportar ciclovías estará disponible próximamente.",
            (effect as MapUiEffect.ShowMessage).message
        )
    }
}
