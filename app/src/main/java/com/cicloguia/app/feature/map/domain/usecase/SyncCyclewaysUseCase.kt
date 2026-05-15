package com.cicloguia.app.feature.map.domain.usecase

import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import com.cicloguia.app.feature.map.domain.repository.CyclewaysRepository
import javax.inject.Inject

class SyncCyclewaysUseCase @Inject constructor(
    private val repository: CyclewaysRepository
) {
    suspend operator fun invoke(): SyncCyclewaysResult = repository.sync()
}