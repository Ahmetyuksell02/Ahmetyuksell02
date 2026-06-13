package com.ahmetyuksell.agent.domain.usecase.chat

import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.model.MessageRole
import com.ahmetyuksell.agent.domain.repository.ConversationRepository
import com.ahmetyuksell.agent.domain.repository.MessageRepository
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        content: String,
        fileIds: List<String> = emptyList()
    ): Message {
        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = MessageRole.USER,
            content = content,
            timestamp = System.currentTimeMillis(),
            fileIds = fileIds
        )
        messageRepository.insertMessage(message)
        conversationRepository.updateTitle(conversationId, content.take(60))
        return message
    }

    suspend fun insertAssistantPlaceholder(
        conversationId: String,
        modelId: String
    ): Message {
        val placeholder = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = MessageRole.ASSISTANT,
            content = "",
            modelId = modelId,
            isStreaming = true,
            timestamp = System.currentTimeMillis()
        )
        messageRepository.insertMessage(placeholder)
        return placeholder
    }
}
