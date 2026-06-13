package com.aiagent.mobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Repository injection and Room query wired in Phase 2
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun searchConversations(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Filtered query wired to Room FTS in Phase 2
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            // Implemented in Phase 2
        }
    }

    fun pinConversation(conversationId: String) {
        viewModelScope.launch {
            // Implemented in Phase 2
        }
    }

    fun refreshConversations() {
        loadConversations()
    }
}
