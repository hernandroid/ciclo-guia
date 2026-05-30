package com.cicloguia.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun applicationId_matchesCurrentAppId() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("com.cicloguia.app", appContext.packageName)
    }
}
