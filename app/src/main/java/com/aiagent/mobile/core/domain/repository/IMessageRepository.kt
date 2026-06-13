package com.aiagent.mobile.core.domain.repository

import com.aiagent.mobile.core.domain.model.Message
import com.aiagent.mobile.core.domain.model.StreamEvent
import kotlinx.coroutines.flow.Flow

interface IMessageRepository {
    fun getByConversation(conversationId: String): Flow<List<Message>>
    suspend fun getById(id: String): Message?
    suspend fun insert(message: Message)
    suspend fun update(message: Message)
    suspend fun delete(messageId: String)

    /**
     * Saves the user message, calls OpenRouter (streaming), saves AI response,
     * updates the conversation's lastMessage, and emits [StreamEvent]s.
     */
    fun streamMessage(
        conversationId: String,
        content: String,
        modelId: String,
        history: List<Message>
    ): Flow<StreamEvent>
}
