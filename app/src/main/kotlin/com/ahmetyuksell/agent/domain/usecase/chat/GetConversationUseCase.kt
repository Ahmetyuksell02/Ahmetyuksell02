package com.ahmetyuksell.agent.domain.usecase.chat

import com.ahmetyuksell.agent.domain.model.Conversation
import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.repository.ConversationRepository
import com.ahmetyuksell.agent.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    operator fun invoke(id: String): Flow<Conversation?> =
        conversationRepository.getConversation(id)

    fun getAll(): Flow<List<Conversation>> =
        conversationRepository.getActiveConversations()
}

class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(conversationId: String): Flow<List<Message>> =
        messageRepository.getMessages(conversationId)
}
