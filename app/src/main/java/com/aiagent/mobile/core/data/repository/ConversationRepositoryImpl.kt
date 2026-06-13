package com.aiagent.mobile.core.data.repository

import com.aiagent.mobile.core.data.local.dao.ConversationDao
import com.aiagent.mobile.core.data.mapper.toDomain
import com.aiagent.mobile.core.data.mapper.toEntity
import com.aiagent.mobile.core.domain.model.Conversation
import com.aiagent.mobile.core.domain.repository.IConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val dao: ConversationDao
) : IConversationRepository {

    override fun getAll(): Flow<List<Conversation>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Conversation>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Conversation?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun insert(conversation: Conversation) =
        dao.insert(conversation.toEntity())

    override suspend fun update(conversation: Conversation) =
        dao.update(conversation.toEntity())

    override suspend fun delete(conversationId: String) =
        dao.delete(conversationId)

    override suspend fun updateTitle(conversationId: String, title: String) =
        dao.updateTitle(conversationId, title)

    override suspend fun updateLastMessage(
        conversationId: String,
        lastMessage: String,
        updatedAt: Long
    ) = dao.updateLastMessage(conversationId, lastMessage, updatedAt)

    override suspend fun updatePinned(conversationId: String, isPinned: Boolean) =
        dao.updatePinned(conversationId, isPinned)
}
