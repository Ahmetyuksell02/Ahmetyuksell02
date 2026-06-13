package com.aiagent.mobile.core.domain.usecase

import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.domain.model.Conversation
import com.aiagent.mobile.core.domain.model.Message
import com.aiagent.mobile.core.domain.model.StreamEvent
import com.aiagent.mobile.core.domain.repository.IConversationRepository
import com.aiagent.mobile.core.domain.repository.IMessageRepository
import com.aiagent.mobile.core.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: IMessageRepository,
    private val conversationRepository: IConversationRepository,
    private val settingsRepository: ISettingsRepository
) {
    /**
     * Ensures a conversation exists (creating it if new), then delegates to the
     * message repository for streaming. Updates the conversation title on the
     * first message using the user's prompt as the title.
     *
     * @return The conversation id used (may be newly created).
     */
    suspend fun prepareConversation(
        conversationId: String,
        modelId: String,
        modelName: String,
        firstUserMessage: String
    ): String {
        if (conversationId != Constants.NEW_CONVERSATION_ID) return conversationId

        val newId = UUID.randomUUID().toString()
        val title = firstUserMessage.take(Constants.AUTO_TITLE_MAX_CHARS)
            .replace("\n", " ")
            .trim()
        val now = System.currentTimeMillis()
        conversationRepository.insert(
            Conversation(
                id = newId,
                title = title.ifBlank { "New Chat" },
                modelId = modelId,
                modelName = modelName,
                createdAt = now,
                updatedAt = now,
                lastMessage = ""
            )
        )
        return newId
    }

    fun stream(
        conversationId: String,
        content: String,
        modelId: String,
        history: List<Message>
    ): Flow<StreamEvent> = messageRepository.streamMessage(
        conversationId = conversationId,
        content = content,
        modelId = modelId,
        history = history
    )
}
