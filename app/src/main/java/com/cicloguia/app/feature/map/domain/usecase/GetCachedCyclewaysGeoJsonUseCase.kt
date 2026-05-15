package com.cicloguia.app.feature.map.domain.usecase

import com.cicloguia.app.feature.map.domain.repository.CyclewaysRepository
import javax.inject.Inject

class GetCachedCyclewaysGeoJsonUseCase @Inject constructor(
    private val repository: CyclewaysRepository
) {
    suspend operator fun invoke(): String? = repository.getCachedGeoJson()
}