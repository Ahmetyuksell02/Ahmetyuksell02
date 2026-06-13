package com.ahmetyuksell.agent.domain.repository

import com.ahmetyuksell.agent.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    fun getActiveConversations(): Flow<List<Conversation>>
    fun getConversation(id: String): Flow<Conversation?>
    suspend fun createConversation(conversation: Conversation): String
    suspend fun updateConversation(conversation: Conversation)
    suspend fun updateTitle(id: String, title: String)
    suspend fun deleteConversation(id: String)
    suspend fun archiveConversation(id: String)
}
