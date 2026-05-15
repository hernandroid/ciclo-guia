package com.cicloguia.app.feature.map.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cicloguia.app.feature.map.domain.model.SyncCyclewaysResult
import com.cicloguia.app.feature.map.domain.usecase.SyncCyclewaysUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncCyclewaysWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncCyclewaysUseCase: SyncCyclewaysUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when (syncCyclewaysUseCase()) {
            SyncCyclewaysResult.AlreadyUpdated,
            SyncCyclewaysResult.Updated -> Result.success()

            is SyncCyclewaysResult.Failed -> Result.retry()
        }
    }
}