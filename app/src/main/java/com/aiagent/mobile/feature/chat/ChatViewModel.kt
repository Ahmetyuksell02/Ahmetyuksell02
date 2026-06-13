package com.aiagent.mobile.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageUiModel(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

data class ChatUiState(
    val conversationTitle: String = "New Chat",
    val modelName: String = Constants.DEFAULT_MODEL_NAME,
    val messages: List<MessageUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conversationId: String =
        savedStateHandle.get<String>(Screen.Chat.ARG_CONVERSATION_ID)
            ?: Constants.NEW_CONVERSATION_ID

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        if (conversationId != Constants.NEW_CONVERSATION_ID) {
            loadConversation(conversationId)
        }
    }

    private fun loadConversation(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Room + repository wired in Phase 2; streaming in Phase 3
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val userMessage = MessageUiModel(
                id = java.util.UUID.randomUUID().toString(),
                content = content.trim(),
                isFromUser = true
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + userMessage,
                    isTyping = true,
                    error = null
                )
            }
            // OpenRouter streaming call wired in Phase 3
            _uiState.update { it.copy(isTyping = false) }
        }
    }

    fun onAttachFile() {
        // File picker + processing wired in Phase 5
    }

    fun onVoiceInput() {
        // Speech-to-text wired in Phase 5
    }

    fun deleteMessage(messageId: String) {
        _uiState.update { state ->
            state.copy(messages = state.messages.filter { it.id != messageId })
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            // Re-submit from edited message wired in Phase 3
        }
    }

    fun retryLastMessage() {
        viewModelScope.launch {
            val lastUserMessage = _uiState.value.messages.lastOrNull { it.isFromUser }
            lastUserMessage?.let { sendMessage(it.content) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
