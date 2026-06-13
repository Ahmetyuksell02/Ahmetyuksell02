package com.ahmetyuksell.agent.data.repository

import com.ahmetyuksell.agent.data.local.dao.MessageDao
import com.ahmetyuksell.agent.data.mapper.toDomain
import com.ahmetyuksell.agent.data.mapper.toEntity
import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val dao: MessageDao
) : MessageRepository {

    override fun getMessages(conversationId: String): Flow<List<Message>> =
        dao.getMessages(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertMessage(message: Message): String {
        dao.insertMessage(message.toEntity())
        return message.id
    }

    override suspend fun updateMessage(message: Message) {
        dao.updateMessage(message.toEntity())
    }

    override suspend fun updateMessageContent(id: String, content: String) {
        dao.updateContent(id, content)
    }

    override suspend fun setStreamingDone(id: String) {
        dao.setStreamingDone(id)
    }

    override suspend fun deleteMessage(id: String) {
        dao.deleteMessage(id)
    }

    override suspend fun getLastMessage(conversationId: String): Message? =
        dao.getLastMessage(conversationId)?.toDomain()
}
