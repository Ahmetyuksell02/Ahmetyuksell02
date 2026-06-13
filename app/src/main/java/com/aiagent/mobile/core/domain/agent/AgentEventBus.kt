package com.aiagent.mobile.core.domain.agent

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.BufferOverflow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<AgentEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<AgentEvent> = _events.asSharedFlow()

    fun tryEmit(event: AgentEvent): Boolean = _events.tryEmit(event)

    suspend fun emit(event: AgentEvent) = _events.emit(event)
}
