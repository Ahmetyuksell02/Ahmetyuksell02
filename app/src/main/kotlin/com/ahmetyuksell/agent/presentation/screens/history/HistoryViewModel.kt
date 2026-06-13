package com.ahmetyuksell.agent.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyuksell.agent.domain.model.Conversation
import com.ahmetyuksell.agent.domain.repository.ConversationRepository
import com.ahmetyuksell.agent.domain.usecase.chat.CreateConversationUseCase
import com.ahmetyuksell.agent.security.SecureKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val createConversationUseCase: CreateConversationUseCase,
    private val secureKeyStore: SecureKeyStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            conversationRepository.getActiveConversations().collect { convs ->
                _uiState.update { it.copy(conversations = convs) }
            }
        }
    }

    fun createNewConversation(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val modelId = secureKeyStore.getDefaultModel() ?: SecureKeyStore.DEFAULT_MODEL
            val id = createConversationUseCase(modelId)
            onCreated(id)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(id)
        }
    }

    fun archiveConversation(id: String) {
        viewModelScope.launch {
            conversationRepository.archiveConversation(id)
        }
    }
}
