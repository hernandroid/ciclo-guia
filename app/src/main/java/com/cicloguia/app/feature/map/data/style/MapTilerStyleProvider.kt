package com.cicloguia.app.feature.map.data.style

class MapTilerStyleProvider(
    private val apiKey: String
) : MapStyleProvider {

    override fun getStyleUrl(): String {
        return "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"
    }
}