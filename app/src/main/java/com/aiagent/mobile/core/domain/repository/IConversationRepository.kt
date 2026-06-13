package com.aiagent.mobile.core.domain.repository

import com.aiagent.mobile.core.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface IConversationRepository {
    fun getAll(): Flow<List<Conversation>>
    fun search(query: String): Flow<List<Conversation>>
    fun getById(id: String): Flow<Conversation?>
    suspend fun insert(conversation: Conversation)
    suspend fun update(conversation: Conversation)
    suspend fun delete(conversationId: String)
    suspend fun updateTitle(conversationId: String, title: String)
    suspend fun updateLastMessage(conversationId: String, lastMessage: String, updatedAt: Long)
    suspend fun updatePinned(conversationId: String, isPinned: Boolean)
}
