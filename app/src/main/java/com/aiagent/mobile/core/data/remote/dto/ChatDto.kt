package com.aiagent.mobile.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Request ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ChatRequestDto(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<ChatMessageDto>,
    @Json(name = "stream") val stream: Boolean = false,
    @Json(name = "max_tokens") val maxTokens: Int? = null,
    @Json(name = "temperature") val temperature: Double? = null,
    @Json(name = "top_p") val topP: Double? = null
)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

// ─── Response ─────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ChatResponseDto(
    @Json(name = "id") val id: String?,
    @Json(name = "model") val model: String?,
    @Json(name = "choices") val choices: List<ChoiceDto>?,
    @Json(name = "usage") val usage: UsageDto?
)

@JsonClass(generateAdapter = true)
data class ChoiceDto(
    @Json(name = "index") val index: Int?,
    @Json(name = "message") val message: ChatMessageDto?,
    @Json(name = "delta") val delta: DeltaDto?,
    @Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class DeltaDto(
    @Json(name = "role") val role: String?,
    @Json(name = "content") val content: String?
)

@JsonClass(generateAdapter = true)
data class UsageDto(
    @Json(name = "prompt_tokens") val promptTokens: Int?,
    @Json(name = "completion_tokens") val completionTokens: Int?,
    @Json(name = "total_tokens") val totalTokens: Int?
)

// ─── Error ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    @Json(name = "error") val error: ApiErrorDetailDto?
)

@JsonClass(generateAdapter = true)
data class ApiErrorDetailDto(
    @Json(name = "message") val message: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "code") val code: String?
)
