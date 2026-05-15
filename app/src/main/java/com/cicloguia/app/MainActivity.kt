package com.cicloguia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cicloguia.app.feature.map.data.style.MapStyleProvider
import com.cicloguia.app.feature.map.presentation.MapScreen
import com.cicloguia.app.core.designsystem.theme.CicloGuiaTheme
import com.cicloguia.app.core.navigation.CicloGuiaNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mapStyleProvider: MapStyleProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            CicloGuiaTheme {
                CicloGuiaNavGraph()
            }
        }
    }
}