package com.ahmetyuksell.agent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamChunkDto(
    val id: String,
    val model: String? = null,
    val choices: List<StreamChoiceDto> = emptyList()
)

@Serializable
data class StreamChoiceDto(
    val index: Int,
    val delta: StreamDeltaDto,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class StreamDeltaDto(
    val role: String? = null,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDeltaDto>? = null
)

@Serializable
data class ToolCallDeltaDto(
    val index: Int,
    val id: String? = null,
    val type: String? = null,
    val function: FunctionDeltaDto? = null
)

@Serializable
data class FunctionDeltaDto(
    val name: String? = null,
    val arguments: String? = null
)
