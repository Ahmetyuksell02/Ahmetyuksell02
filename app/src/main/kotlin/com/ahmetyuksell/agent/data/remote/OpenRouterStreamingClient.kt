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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterStreamingClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    // ConcurrentHashMap: stream() and cancelStream() may be called from different threads
    private val activeSources = ConcurrentHashMap<String, EventSource>()

    // Derived client with a 3-minute call-level timeout; inherits connect/read from shared client.
    // A separate instance is required so we don't mutate the shared OkHttpClient.
    private val streamingClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .callTimeout(180, TimeUnit.SECONDS)
            .build()
    }

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
                    // Partial or malformed SSE line — skip rather than closing the stream.
                    // OpenRouter may emit keep-alive comments or split delta lines.
                    Timber.w("Failed to parse SSE chunk (${data.length}B): ${data.take(100)}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val code = response?.code
                Timber.e(t, "SSE stream failure: HTTP $code streamId=$streamId")
                channel.close(t ?: Exception("SSE stream failed: $code"))
            }

            override fun onClosed(eventSource: EventSource) {
                channel.close()
            }
        }

        val eventSource = EventSources.createFactory(streamingClient)
            .newEventSource(request, listener)
        activeSources[streamId] = eventSource
        Timber.d("SSE stream started: streamId=$streamId")

        awaitClose {
            eventSource.cancel()
            activeSources.remove(streamId)
            Timber.d("SSE stream closed: streamId=$streamId")
        }
    }

    fun cancelStream(streamId: String) {
        activeSources.remove(streamId)?.cancel()
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
