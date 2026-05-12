package com.cicloguia.app.domain.cycleways.model

sealed interface SyncCyclewaysResult {
    data object AlreadyUpdated : SyncCyclewaysResult
    data object Updated : SyncCyclewaysResult
    data class Failed(val error: Throwable) : SyncCyclewaysResult
}