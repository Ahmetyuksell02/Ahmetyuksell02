package com.ahmetyuksell.agent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatCompletionRequestDto(
    val model: String,
    val messages: List<MessageDto>,
    val stream: Boolean = false,
    val tools: List<ToolDto>? = null,
    @SerialName("tool_choice") val toolChoice: String? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val temperature: Double? = null
)

@Serializable
data class MessageDto(
    val role: String,
    val content: JsonElement,
    @SerialName("tool_call_id") val toolCallId: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null
)

@Serializable
data class ContentPartDto(
    val type: String,
    val text: String? = null,
    @SerialName("image_url") val imageUrl: ImageUrlDto? = null
)

@Serializable
data class ImageUrlDto(
    val url: String,
    val detail: String = "auto"
)

@Serializable
data class ToolCallDto(
    val id: String,
    val type: String = "function",
    val function: FunctionCallDto
)

@Serializable
data class FunctionCallDto(
    val name: String,
    val arguments: String
)
