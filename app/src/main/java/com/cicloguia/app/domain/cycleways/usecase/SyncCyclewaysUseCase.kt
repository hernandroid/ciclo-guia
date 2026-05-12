package com.cicloguia.app.domain.cycleways.usecase

import com.cicloguia.app.domain.cycleways.model.SyncCyclewaysResult
import com.cicloguia.app.domain.cycleways.repository.CyclewaysRepository
import javax.inject.Inject

class SyncCyclewaysUseCase @Inject constructor(
    private val repository: CyclewaysRepository
) {
    suspend operator fun invoke(): SyncCyclewaysResult = repository.sync()
}