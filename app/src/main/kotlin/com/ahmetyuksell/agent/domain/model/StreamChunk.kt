package com.ahmetyuksell.agent.domain.model

data class StreamChunk(
    val id: String,
    val content: String? = null,
    val toolCallDeltas: List<ToolCallDelta> = emptyList(),
    val finishReason: String? = null
)

data class ToolCallDelta(
    val index: Int,
    val id: String? = null,
    val name: String? = null,
    val argumentsDelta: String? = null
)
