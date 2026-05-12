package com.cicloguia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cicloguia.app.core.map.MapStyleProvider
import com.cicloguia.app.feature.map.MapScreen
import com.cicloguia.app.ui.theme.CicloGuiaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mapStyleProvider: MapStyleProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CicloGuiaTheme {
                MapScreen(
                    mapStyleProvider = mapStyleProvider
                )
            }
        }
    }
}