package com.ahmetyuksell.agent.domain.usecase.chat

import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.model.MessageRole
import com.ahmetyuksell.agent.domain.model.StreamChunk
import com.ahmetyuksell.agent.domain.repository.AiModelRepository
import com.ahmetyuksell.agent.domain.repository.MessageRepository
import com.ahmetyuksell.agent.data.remote.OpenRouterStreamingClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class StreamCompletionUseCase @Inject constructor(
    private val streamingClient: OpenRouterStreamingClient,
    private val messageRepository: MessageRepository,
    private val aiModelRepository: AiModelRepository
) {
    operator fun invoke(
        messages: List<Message>,
        modelId: String,
        placeholderMessageId: String,
        tools: List<String> = emptyList()
    ): Flow<StreamChunk> {
        val buffer = StringBuilder()

        return streamingClient.stream(
            messages = messages,
            modelId = modelId,
            tools = tools
        ).onEach { chunk ->
            chunk.content?.let { delta ->
                buffer.append(delta)
                messageRepository.updateMessageContent(placeholderMessageId, buffer.toString())
            }
        }.onCompletion { cause ->
            if (cause == null) {
                messageRepository.setStreamingDone(placeholderMessageId)
            }
        }
    }
}
