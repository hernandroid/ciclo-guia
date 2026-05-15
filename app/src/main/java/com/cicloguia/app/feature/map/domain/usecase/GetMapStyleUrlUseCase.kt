package com.cicloguia.app.feature.map.domain.usecase

import com.cicloguia.app.feature.map.data.style.MapStyleProvider
import javax.inject.Inject

class GetMapStyleUrlUseCase @Inject constructor(
    private val mapStyleProvider: MapStyleProvider
) {
    operator fun invoke(): String {
        return mapStyleProvider.getStyleUrl()
    }
}