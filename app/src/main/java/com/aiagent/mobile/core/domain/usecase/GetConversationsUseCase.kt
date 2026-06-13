package com.aiagent.mobile.core.domain.usecase

import com.aiagent.mobile.core.domain.model.Conversation
import com.aiagent.mobile.core.domain.repository.IConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val repository: IConversationRepository
) {
    operator fun invoke(): Flow<List<Conversation>> = repository.getAll()

    fun search(query: String): Flow<List<Conversation>> =
        if (query.isBlank()) repository.getAll()
        else repository.search(query)
}
