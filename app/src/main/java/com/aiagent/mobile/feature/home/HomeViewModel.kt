package com.aiagent.mobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.domain.model.Conversation
import com.aiagent.mobile.core.domain.usecase.GetConversationsUseCase
import com.aiagent.mobile.core.domain.repository.IConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ConversationUiModel(
    val id: String,
    val title: String,
    val lastMessage: String,
    val modelName: String,
    val formattedTime: String,
    val isPinned: Boolean = false,
    val folderId: String? = null
)

data class HomeUiState(
    val conversations: List<ConversationUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val conversationRepository: IConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeConversations()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            getConversationsUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { conversations ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversations = conversations.map { c -> c.toUiModel() }
                        )
                    }
                }
        }
    }

    fun searchConversations(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            getConversationsUseCase.search(query)
                .catch { /* ignore search errors */ }
                .collect { conversations ->
                    _uiState.update {
                        it.copy(conversations = conversations.map { c -> c.toUiModel() })
                    }
                }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.delete(conversationId)
        }
    }

    fun pinConversation(conversationId: String, isPinned: Boolean) {
        viewModelScope.launch {
            conversationRepository.updatePinned(conversationId, isPinned)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun Conversation.toUiModel(): ConversationUiModel {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
        val now = System.currentTimeMillis()
        val isToday = (now - updatedAt) < 86_400_000L
        return ConversationUiModel(
            id = id,
            title = title,
            lastMessage = lastMessage.ifBlank { "No messages yet" },
            modelName = modelName,
            formattedTime = if (isToday) formatter.format(Date(updatedAt))
                           else dateFormatter.format(Date(updatedAt)),
            isPinned = isPinned,
            folderId = folderId
        )
    }
}
