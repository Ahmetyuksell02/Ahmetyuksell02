package com.ahmetyuksell.agent.presentation.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyuksell.agent.audio.SpeechEvent
import com.ahmetyuksell.agent.audio.SpeechRecognitionManager
import com.ahmetyuksell.agent.audio.TextToSpeechManager
import com.ahmetyuksell.agent.concurrency.AgentConcurrencyManager
import com.ahmetyuksell.agent.concurrency.RateLimiter
import com.ahmetyuksell.agent.domain.usecase.chat.*
import com.ahmetyuksell.agent.domain.usecase.model.GetAvailableModelsUseCase
import com.ahmetyuksell.agent.observability.AppLogger
import com.ahmetyuksell.agent.security.SecureKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sendMessageUseCase: SendMessageUseCase,
    private val streamCompletionUseCase: StreamCompletionUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase,
    private val speechRecognitionManager: SpeechRecognitionManager,
    private val textToSpeechManager: TextToSpeechManager,
    private val rateLimiter: RateLimiter,
    private val concurrencyManager: AgentConcurrencyManager,
    private val appLogger: AppLogger,
    private val secureKeyStore: SecureKeyStore
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null
    private var voiceJob: Job? = null

    init {
        observeConversation()
        observeMessages()
        observeModels()
    }

    private fun observeConversation() {
        viewModelScope.launch {
            getConversationUseCase(conversationId).collect { conv ->
                _uiState.update { it.copy(conversation = conv) }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            getMessagesUseCase(conversationId).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    private fun observeModels() {
        viewModelScope.launch {
            getAvailableModelsUseCase.getEnabled().collect { models ->
                val current = _uiState.value.selectedModel
                val selected = models.find { it.id == current?.id }
                    ?: models.firstOrNull()
                    ?: models.find { it.id == secureKeyStore.getDefaultModel() }
                _uiState.update { it.copy(availableModels = models, selectedModel = selected) }
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onModelSelected(modelId: String) {
        val model = _uiState.value.availableModels.find { it.id == modelId }
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isStreaming) return

        val modelId = _uiState.value.selectedModel?.id ?: SecureKeyStore.DEFAULT_MODEL

        _uiState.update { it.copy(inputText = "", isStreaming = true, error = null) }
        textToSpeechManager.stop()

        streamJob = viewModelScope.launch {
            try {
                rateLimiter.acquire("chat")
                val start = System.currentTimeMillis()

                val userMessage = sendMessageUseCase(conversationId, text)
                val placeholder = sendMessageUseCase.insertAssistantPlaceholder(conversationId, modelId)

                val allMessages = _uiState.value.messages

                var tokenCount = 0
                concurrencyManager.withStreamSlot(conversationId) {
                    streamCompletionUseCase(allMessages, modelId, placeholder.id)
                        .collect { chunk ->
                            chunk.content?.let { tokenCount++ }
                        }
                }

                val duration = System.currentTimeMillis() - start
                appLogger.logApiCall("chat/completions", duration, true)
                appLogger.logStreamEvent("stream_complete", tokenCount)

                val lastMessage = _uiState.value.messages.lastOrNull()
                lastMessage?.content?.let { response ->
                    if (response.isNotBlank()) {
                        val sentences = textToSpeechManager.splitIntoSentences(response)
                        sentences.forEach { textToSpeechManager.speakQueued(it) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Streaming error")
                appLogger.logApiCall("chat/completions", 0, false)
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isStreaming = false) }
            }
        }
    }

    fun cancelStreaming() {
        streamJob?.cancel()
        _uiState.update { it.copy(isStreaming = false) }
    }

    fun startVoiceInput() {
        if (_uiState.value.isVoiceListening) return
        textToSpeechManager.stop()
        _uiState.update { it.copy(isVoiceListening = true, voiceInterimText = "") }

        voiceJob = viewModelScope.launch {
            speechRecognitionManager.startListening().collect { event ->
                when (event) {
                    is SpeechEvent.PartialResult ->
                        _uiState.update { it.copy(voiceInterimText = event.text) }

                    is SpeechEvent.FinalResult -> {
                        _uiState.update {
                            it.copy(
                                inputText = event.text,
                                voiceInterimText = "",
                                isVoiceListening = false
                            )
                        }
                        if (event.text.isNotBlank()) sendMessage()
                    }

                    is SpeechEvent.Error -> {
                        _uiState.update { it.copy(isVoiceListening = false, voiceInterimText = "", error = event.message) }
                    }

                    is SpeechEvent.Stopped ->
                        _uiState.update { it.copy(isVoiceListening = false, voiceInterimText = "") }

                    is SpeechEvent.Ready -> Unit
                }
            }
        }
    }

    fun stopVoiceInput() {
        speechRecognitionManager.stopListening()
        voiceJob?.cancel()
        _uiState.update { it.copy(isVoiceListening = false, voiceInterimText = "") }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        textToSpeechManager.stop()
        streamJob?.cancel()
        voiceJob?.cancel()
        super.onCleared()
    }
}
