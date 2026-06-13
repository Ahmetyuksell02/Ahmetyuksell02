package com.ahmetyuksell.agent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatCompletionResponseDto(
    val id: String,
    val model: String,
    val choices: List<ChoiceDto>,
    val usage: UsageDto? = null
)

@Serializable
data class ChoiceDto(
    val index: Int,
    val message: MessageResponseDto,
    @SerialName("finish_reason") val finishReason: String?
)

@Serializable
data class MessageResponseDto(
    val role: String,
    val content: String?,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null
)

@Serializable
data class UsageDto(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)
