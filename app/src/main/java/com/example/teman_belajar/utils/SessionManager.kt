package com.example.teman_belajar.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {
    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(replay = 0)
    val sessionExpiredEvent = _sessionExpiredEvent.asSharedFlow()

    suspend fun triggerSessionExpired() {
        _sessionExpiredEvent.emit(Unit)
    }
}