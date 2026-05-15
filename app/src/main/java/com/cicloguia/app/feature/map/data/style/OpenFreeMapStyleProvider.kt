package com.cicloguia.app.feature.map.data.style

import javax.inject.Inject

class OpenFreeMapStyleProvider @Inject constructor() : MapStyleProvider {
    override fun getStyleUrl(): String {
        return "https://tiles.openfreemap.org/styles/liberty"
    }
}