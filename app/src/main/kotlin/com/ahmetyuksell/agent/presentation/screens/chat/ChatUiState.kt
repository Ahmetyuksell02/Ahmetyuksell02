package com.ahmetyuksell.agent.presentation.screens.chat

import com.ahmetyuksell.agent.domain.model.AiModel
import com.ahmetyuksell.agent.domain.model.Conversation
import com.ahmetyuksell.agent.domain.model.Message

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val availableModels: List<AiModel> = emptyList(),
    val selectedModel: AiModel? = null,
    val isVoiceListening: Boolean = false,
    val isTtsSpeaking: Boolean = false,
    val error: String? = null,
    val voiceInterimText: String = ""
)
