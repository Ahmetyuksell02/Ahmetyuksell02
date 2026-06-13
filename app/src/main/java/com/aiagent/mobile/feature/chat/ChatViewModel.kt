package com.aiagent.mobile.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.domain.model.AIModel
import com.aiagent.mobile.core.domain.model.Message
import com.aiagent.mobile.core.domain.model.MessageRole
import com.aiagent.mobile.core.domain.model.StreamEvent
import com.aiagent.mobile.core.domain.repository.IConversationRepository
import com.aiagent.mobile.core.domain.repository.IMessageRepository
import com.aiagent.mobile.core.domain.repository.ISettingsRepository
import com.aiagent.mobile.core.domain.usecase.GetModelsUseCase
import com.aiagent.mobile.core.domain.usecase.SendMessageUseCase
import com.aiagent.mobile.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MessageUiModel(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

data class ChatUiState(
    val conversationId: String = Constants.NEW_CONVERSATION_ID,
    val conversationTitle: String = "New Chat",
    val modelId: String = Constants.DEFAULT_MODEL_ID,
    val modelName: String = Constants.DEFAULT_MODEL_NAME,
    val messages: List<MessageUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val streamingMessageId: String? = null,
    val error: String? = null,
    val availableModels: List<AIModel> = emptyList(),
    val isLoadingModels: Boolean = false,
    val searchQuery: String = "",
    val searchResultIds: Set<String> = emptySet(),
    val editingMessageId: String? = null,
    val editingContent: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sendMessageUseCase: SendMessageUseCase,
    private val messageRepository: IMessageRepository,
    private val conversationRepository: IConversationRepository,
    private val settingsRepository: ISettingsRepository,
    private val getModelsUseCase: GetModelsUseCase
) : ViewModel() {

    private val navConversationId: String =
        savedStateHandle.get<String>(Screen.Chat.ARG_CONVERSATION_ID)
            ?: Constants.NEW_CONVERSATION_ID

    // Tracks the actual (potentially newly created) conversation id
    private var activeConversationId: String = navConversationId

    // Domain messages for building the API request history
    private val domainMessages = mutableListOf<Message>()

    private val _uiState = MutableStateFlow(ChatUiState(conversationId = navConversationId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadDefaultModel()
        loadModels()
        if (navConversationId != Constants.NEW_CONVERSATION_ID) {
            loadConversation(navConversationId)
            observeMessages(navConversationId)
        }
    }

    private fun loadDefaultModel() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        modelId = settings.defaultModelId,
                        modelName = settings.defaultModelName
                    )
                }
            }
        }
    }

    private fun loadConversation(id: String) {
        viewModelScope.launch {
            conversationRepository.getById(id).collect { conv ->
                conv?.let {
                    _uiState.update { s ->
                        s.copy(
                            conversationTitle = it.title,
                            modelId = it.modelId,
                            modelName = it.modelName
                        )
                    }
                }
            }
        }
    }

    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            messageRepository.getByConversation(conversationId)
                .catch { /* non-fatal: messages already visible if loaded */ }
                .collect { messages ->
                    domainMessages.clear()
                    domainMessages.addAll(messages)
                    _uiState.update { state ->
                        state.copy(
                            messages = messages.map { msg ->
                                MessageUiModel(
                                    id = msg.id,
                                    content = msg.content,
                                    isFromUser = msg.role == MessageRole.USER,
                                    timestamp = msg.timestamp,
                                    isError = msg.isError
                                )
                            }
                        )
                    }
                }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isTyping) return

        viewModelScope.launch {
            val currentState = _uiState.value

            // Ensure conversation exists (creates one if new)
            if (activeConversationId == Constants.NEW_CONVERSATION_ID) {
                activeConversationId = sendMessageUseCase.prepareConversation(
                    conversationId = Constants.NEW_CONVERSATION_ID,
                    modelId = currentState.modelId,
                    modelName = currentState.modelName,
                    firstUserMessage = content
                )
                _uiState.update {
                    it.copy(
                        conversationId = activeConversationId,
                        conversationTitle = content.take(Constants.AUTO_TITLE_MAX_CHARS)
                    )
                }
            }

            val streamingId = UUID.randomUUID().toString()
            _uiState.update { it.copy(isTyping = true, error = null, streamingMessageId = streamingId) }

            sendMessageUseCase.stream(
                conversationId = activeConversationId,
                content = content,
                modelId = currentState.modelId,
                history = domainMessages.toList()
            ).catch { e ->
                _uiState.update { it.copy(isTyping = false, streamingMessageId = null, error = e.message) }
            }.collect { event ->
                handleStreamEvent(event, streamingId)
            }
        }
    }

    private fun handleStreamEvent(event: StreamEvent, streamingId: String) {
        when (event) {
            is StreamEvent.UserMessageSaved -> {
                domainMessages.add(event.message)
                val uiMsg = MessageUiModel(
                    id = event.message.id,
                    content = event.message.content,
                    isFromUser = true,
                    timestamp = event.message.timestamp
                )
                // Add streaming placeholder for AI response
                val placeholder = MessageUiModel(
                    id = streamingId,
                    content = "",
                    isFromUser = false,
                    isStreaming = true
                )
                _uiState.update {
                    it.copy(messages = it.messages + uiMsg + placeholder)
                }
            }

            is StreamEvent.Chunk -> {
                _uiState.update { state ->
                    val updated = state.messages.map { msg ->
                        if (msg.id == streamingId) msg.copy(content = msg.content + event.content)
                        else msg
                    }
                    state.copy(messages = updated)
                }
            }

            is StreamEvent.Complete -> {
                domainMessages.add(event.message)
                _uiState.update { state ->
                    val updated = state.messages.map { msg ->
                        if (msg.id == streamingId) {
                            MessageUiModel(
                                id = event.message.id,
                                content = event.message.content,
                                isFromUser = false,
                                timestamp = event.message.timestamp,
                                isStreaming = false
                            )
                        } else msg
                    }
                    state.copy(
                        messages = updated,
                        isTyping = false,
                        streamingMessageId = null
                    )
                }
            }

            is StreamEvent.Error -> {
                // Replace streaming placeholder with error message
                _uiState.update { state ->
                    val updated = state.messages.map { msg ->
                        if (msg.id == streamingId) {
                            msg.copy(content = event.message, isStreaming = false, isError = true)
                        } else msg
                    }
                    state.copy(
                        messages = updated,
                        isTyping = false,
                        streamingMessageId = null,
                        error = event.message
                    )
                }
            }
        }
    }

    private fun loadModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModels = true) }
            when (val result = getModelsUseCase()) {
                is com.aiagent.mobile.core.common.Resource.Success ->
                    _uiState.update { it.copy(availableModels = result.data, isLoadingModels = false) }
                else ->
                    _uiState.update { it.copy(isLoadingModels = false) }
            }
        }
    }

    fun refreshModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModels = true) }
            when (val result = getModelsUseCase(forceRefresh = true)) {
                is com.aiagent.mobile.core.common.Resource.Success ->
                    _uiState.update { it.copy(availableModels = result.data, isLoadingModels = false) }
                else ->
                    _uiState.update { it.copy(isLoadingModels = false) }
            }
        }
    }

    fun selectModel(modelId: String, modelName: String) {
        _uiState.update { it.copy(modelId = modelId, modelName = modelName) }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            messageRepository.delete(messageId)
        }
        _uiState.update { state ->
            state.copy(messages = state.messages.filter { it.id != messageId })
        }
    }

    fun retryLastMessage() {
        val lastUserMessage = _uiState.value.messages.lastOrNull { it.isFromUser }
        lastUserMessage?.let { sendMessage(it.content) }
    }

    fun searchMessages(query: String) {
        val q = query.trim()
        _uiState.update { state ->
            val ids = if (q.isBlank()) emptySet()
            else state.messages
                .filter { it.content.contains(q, ignoreCase = true) }
                .map { it.id }
                .toSet()
            state.copy(searchQuery = q, searchResultIds = ids)
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResultIds = emptySet()) }
    }

    fun startEditMessage(messageId: String) {
        val message = _uiState.value.messages.find { it.id == messageId } ?: return
        _uiState.update { it.copy(editingMessageId = messageId, editingContent = message.content) }
    }

    fun updateEditContent(content: String) {
        _uiState.update { it.copy(editingContent = content) }
    }

    fun submitEdit() {
        val state = _uiState.value
        val id = state.editingMessageId ?: return
        val newContent = state.editingContent.trim()
        if (newContent.isBlank()) return
        viewModelScope.launch {
            val original = messageRepository.getById(id) ?: return@launch
            messageRepository.update(original.copy(content = newContent))
        }
        _uiState.update { s ->
            s.copy(
                messages = s.messages.map { if (it.id == id) it.copy(content = newContent) else it },
                editingMessageId = null,
                editingContent = ""
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingMessageId = null, editingContent = "") }
    }

    fun onAttachFile() {
        // File picker + processing wired in Phase 5
    }

    fun onVoiceInput() {
        // STT wired in Phase 5
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
