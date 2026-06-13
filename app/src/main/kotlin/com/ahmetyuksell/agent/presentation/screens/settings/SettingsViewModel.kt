package com.ahmetyuksell.agent.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyuksell.agent.domain.model.AiModel
import com.ahmetyuksell.agent.domain.usecase.model.GetAvailableModelsUseCase
import com.ahmetyuksell.agent.observability.MetricsTracker
import com.ahmetyuksell.agent.security.SecureKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val defaultModelId: String = SecureKeyStore.DEFAULT_MODEL,
    val availableModels: List<AiModel> = emptyList(),
    val metricsSummary: String = "",
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureKeyStore: SecureKeyStore,
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase,
    private val metricsTracker: MetricsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                hasApiKey = secureKeyStore.hasApiKey(),
                defaultModelId = secureKeyStore.getDefaultModel() ?: SecureKeyStore.DEFAULT_MODEL
            )
        }

        viewModelScope.launch {
            getAvailableModelsUseCase.getAll().collect { models ->
                _uiState.update { it.copy(availableModels = models) }
            }
        }
    }

    fun onApiKeyChanged(key: String) {
        _uiState.update { it.copy(apiKey = key) }
    }

    fun saveApiKey() {
        val key = _uiState.value.apiKey.trim()
        if (key.isBlank()) return
        secureKeyStore.saveApiKey(key)
        _uiState.update { it.copy(apiKey = "", hasApiKey = true, syncMessage = "API key saved") }
    }

    fun clearApiKey() {
        secureKeyStore.clearApiKey()
        _uiState.update { it.copy(hasApiKey = false, syncMessage = "API key cleared") }
    }

    fun setDefaultModel(modelId: String) {
        secureKeyStore.saveDefaultModel(modelId)
        _uiState.update { it.copy(defaultModelId = modelId) }
    }

    fun syncModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
            val result = getAvailableModelsUseCase.sync()
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncMessage = result.toString()
                )
            }
        }
    }

    fun refreshMetrics() {
        _uiState.update { it.copy(metricsSummary = metricsTracker.getSummary()) }
    }

    fun dismissSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }
}
