package com.aiagent.mobile.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.domain.repository.ISettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val defaultModelId: String = Constants.DEFAULT_MODEL_ID,
    val defaultModelName: String = Constants.DEFAULT_MODEL_NAME,
    val isDarkTheme: Boolean = false,
    val fontSize: Float = Constants.DEFAULT_FONT_SIZE,
    val notificationsEnabled: Boolean = true,
    val ttsSpeed: Float = Constants.DEFAULT_TTS_SPEED,
    val ttsLanguage: String = Constants.DEFAULT_TTS_LANGUAGE,
    val isApiKeyVisible: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: ISettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // Pre-populate the API key field (masked) from encrypted prefs
        val storedKey = settingsRepository.getApiKey()
        _uiState.update { it.copy(apiKey = storedKey) }

        viewModelScope.launch {
            settingsRepository.getSettings()
                .catch { /* use defaults on error */ }
                .collect { settings ->
                    _uiState.update {
                        it.copy(
                            defaultModelId = settings.defaultModelId,
                            defaultModelName = settings.defaultModelName,
                            isDarkTheme = settings.isDarkTheme,
                            fontSize = settings.fontSize,
                            notificationsEnabled = settings.notificationsEnabled,
                            ttsSpeed = settings.ttsSpeed,
                            ttsLanguage = settings.ttsLanguage
                        )
                    }
                }
        }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun toggleApiKeyVisibility() {
        _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
    }

    fun saveApiKey() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                settingsRepository.saveApiKey(_uiState.value.apiKey)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Failed to save API key: ${e.message}")
                }
            }
        }
    }

    fun updateDarkTheme(enabled: Boolean) {
        _uiState.update { it.copy(isDarkTheme = enabled) }
        viewModelScope.launch { settingsRepository.updateDarkTheme(enabled) }
    }

    fun updateFontSize(size: Float) {
        _uiState.update { it.copy(fontSize = size) }
        viewModelScope.launch { settingsRepository.updateFontSize(size) }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch { settingsRepository.updateNotificationsEnabled(enabled) }
    }

    fun updateTtsSpeed(speed: Float) {
        _uiState.update { it.copy(ttsSpeed = speed) }
        viewModelScope.launch { settingsRepository.updateTtsSpeed(speed) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
