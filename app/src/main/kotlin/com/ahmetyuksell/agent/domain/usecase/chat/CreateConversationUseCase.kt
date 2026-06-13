package com.ahmetyuksell.agent.domain.usecase.chat

import com.ahmetyuksell.agent.domain.model.Conversation
import com.ahmetyuksell.agent.domain.repository.ConversationRepository
import java.util.UUID
import javax.inject.Inject

class CreateConversationUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(modelId: String, systemPrompt: String? = null): String {
        val now = System.currentTimeMillis()
        val conversation = Conversation(
            id = UUID.randomUUID().toString(),
            title = "New conversation",
            modelId = modelId,
            systemPrompt = systemPrompt,
            createdAt = now,
            updatedAt = now
        )
        return conversationRepository.createConversation(conversation)
    }
}
