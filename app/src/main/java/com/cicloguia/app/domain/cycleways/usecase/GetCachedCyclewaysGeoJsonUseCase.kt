package com.cicloguia.app.domain.cycleways.usecase

import com.cicloguia.app.domain.cycleways.repository.CyclewaysRepository
import javax.inject.Inject

class GetCachedCyclewaysGeoJsonUseCase @Inject constructor(
    private val repository: CyclewaysRepository
) {
    suspend operator fun invoke(): String? = repository.getCachedGeoJson()
}