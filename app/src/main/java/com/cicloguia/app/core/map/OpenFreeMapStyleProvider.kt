package com.cicloguia.app.core.map

class OpenFreeMapStyleProvider : MapStyleProvider {
    override fun getStyleUrl(): String {
        return "https://tiles.openfreemap.org/styles/liberty"
    }
}