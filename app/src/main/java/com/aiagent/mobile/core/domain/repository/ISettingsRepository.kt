package com.aiagent.mobile.core.domain.repository

import com.aiagent.mobile.core.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun getSettings(): Flow<UserSettings>
    fun getApiKey(): String
    suspend fun saveApiKey(apiKey: String)
    suspend fun clearApiKey()
    suspend fun updateDefaultModel(modelId: String, modelName: String)
    suspend fun updateDarkTheme(enabled: Boolean)
    suspend fun updateFontSize(size: Float)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateTtsSpeed(speed: Float)
    suspend fun updateTtsLanguage(language: String)
}
