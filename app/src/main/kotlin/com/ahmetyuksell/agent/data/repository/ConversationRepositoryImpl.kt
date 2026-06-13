package com.ahmetyuksell.agent.data.repository

import com.ahmetyuksell.agent.data.local.dao.ConversationDao
import com.ahmetyuksell.agent.data.mapper.toDomain
import com.ahmetyuksell.agent.data.mapper.toEntity
import com.ahmetyuksell.agent.domain.model.Conversation
import com.ahmetyuksell.agent.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val dao: ConversationDao
) : ConversationRepository {

    override fun getAllConversations(): Flow<List<Conversation>> =
        dao.getAllConversations().map { list -> list.map { it.toDomain() } }

    override fun getActiveConversations(): Flow<List<Conversation>> =
        dao.getActiveConversations().map { list -> list.map { it.toDomain() } }

    override fun getConversation(id: String): Flow<Conversation?> =
        dao.getConversationById(id).map { it?.toDomain() }

    override suspend fun createConversation(conversation: Conversation): String {
        dao.insertConversation(conversation.toEntity())
        return conversation.id
    }

    override suspend fun updateConversation(conversation: Conversation) {
        dao.updateConversation(conversation.toEntity())
    }

    override suspend fun updateTitle(id: String, title: String) {
        dao.updateTitle(id, title, System.currentTimeMillis())
    }

    override suspend fun deleteConversation(id: String) {
        dao.deleteConversation(id)
    }

    override suspend fun archiveConversation(id: String) {
        dao.archiveConversation(id, System.currentTimeMillis())
    }
}
