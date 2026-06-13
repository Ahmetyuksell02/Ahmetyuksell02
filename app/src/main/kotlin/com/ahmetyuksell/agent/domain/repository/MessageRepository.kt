package com.ahmetyuksell.agent.domain.repository

import com.ahmetyuksell.agent.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessages(conversationId: String): Flow<List<Message>>
    suspend fun insertMessage(message: Message): String
    suspend fun updateMessage(message: Message)
    suspend fun updateMessageContent(id: String, content: String)
    suspend fun setStreamingDone(id: String)
    suspend fun deleteMessage(id: String)
    suspend fun getLastMessage(conversationId: String): Message?
}
