package com.ahmetyuksell.agent.data.remote

import com.ahmetyuksell.agent.data.remote.dto.ChatCompletionRequestDto
import com.ahmetyuksell.agent.data.remote.dto.MessageDto
import com.ahmetyuksell.agent.data.remote.dto.StreamChunkDto
import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.model.MessageRole
import com.ahmetyuksell.agent.domain.model.StreamChunk
import com.ahmetyuksell.agent.domain.model.ToolCallDelta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterStreamingClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private val activeSources = mutableMapOf<String, EventSource>()

    fun stream(
        messages: List<Message>,
        modelId: String,
        tools: List<String> = emptyList(),
        streamId: String = System.currentTimeMillis().toString()
    ): Flow<StreamChunk> = callbackFlow {
        val requestDto = ChatCompletionRequestDto(
            model = modelId,
            messages = messages.map { it.toDto() },
            stream = true
        )

        val body = json.encodeToString(requestDto)
            .toRequestBody("application/json".toMediaType())

        // Auth header injected by AuthInterceptor on the shared OkHttpClient
        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .post(body)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    channel.close()
                    return
                }
                try {
                    val chunkDto = json.decodeFromString<StreamChunkDto>(data)
                    trySend(chunkDto.toStreamChunk())
                } catch (e: Exception) {
                    Timber.w("Failed to parse SSE chunk: ${data.take(200)}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                channel.close(t ?: Exception("SSE stream failed: ${response?.code}"))
            }

            override fun onClosed(eventSource: EventSource) {
                channel.close()
            }
        }

        val eventSource = EventSources.createFactory(okHttpClient).newEventSource(request, listener)
        activeSources[streamId] = eventSource

        awaitClose {
            eventSource.cancel()
            activeSources.remove(streamId)
        }
    }

    fun cancelStream(streamId: String) {
        activeSources[streamId]?.cancel()
        activeSources.remove(streamId)
    }

    private fun Message.toDto(): MessageDto {
        val contentJson: JsonElement = when {
            fileIds.isNotEmpty() && role == MessageRole.USER -> buildJsonArray {
                add(buildJsonObject { put("type", "text"); put("text", content) })
            }
            else -> JsonPrimitive(content)
        }
        return MessageDto(role = role.apiValue(), content = contentJson, toolCallId = toolCallId)
    }

    private fun StreamChunkDto.toStreamChunk(): StreamChunk {
        val choice = choices.firstOrNull()
        return StreamChunk(
            id = id,
            content = choice?.delta?.content,
            toolCallDeltas = choice?.delta?.toolCalls?.map { delta ->
                ToolCallDelta(
                    index = delta.index,
                    id = delta.id,
                    name = delta.function?.name,
                    argumentsDelta = delta.function?.arguments
                )
            } ?: emptyList(),
            finishReason = choice?.finishReason
        )
    }
}
