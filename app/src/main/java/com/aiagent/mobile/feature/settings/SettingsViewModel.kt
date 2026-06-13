package com.aiagent.mobile.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.common.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val apiKeyMasked: String = "",
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
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // DataStore + EncryptedSharedPreferences wired in Phase 2
        }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update {
            it.copy(
                apiKey = apiKey,
                apiKeyMasked = maskApiKey(apiKey)
            )
        }
    }

    fun toggleApiKeyVisibility() {
        _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
    }

    fun updateDarkTheme(enabled: Boolean) {
        _uiState.update { it.copy(isDarkTheme = enabled) }
        viewModelScope.launch { /* persist via DataStore in Phase 2 */ }
    }

    fun updateFontSize(size: Float) {
        _uiState.update { it.copy(fontSize = size) }
        viewModelScope.launch { /* persist via DataStore in Phase 2 */ }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch { /* persist via DataStore in Phase 2 */ }
    }

    fun updateTtsSpeed(speed: Float) {
        _uiState.update { it.copy(ttsSpeed = speed) }
        viewModelScope.launch { /* persist via DataStore in Phase 2 */ }
    }

    fun saveApiKey() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            // Android Keystore + EncryptedSharedPreferences wired in Phase 6
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun maskApiKey(key: String): String {
        if (key.length <= 8) return "•".repeat(key.length)
        return key.take(4) + "•".repeat(key.length - 8) + key.takeLast(4)
    }
}
