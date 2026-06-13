package com.aiagent.mobile.core.data.repository

import com.aiagent.mobile.core.data.preferences.EncryptedPreferencesManager
import com.aiagent.mobile.core.data.preferences.UserPreferencesDataStore
import com.aiagent.mobile.core.domain.model.UserSettings
import com.aiagent.mobile.core.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val encryptedPrefs: EncryptedPreferencesManager
) : ISettingsRepository {

    override fun getSettings(): Flow<UserSettings> = dataStore.settings

    override fun getApiKey(): String = encryptedPrefs.getApiKey()

    override suspend fun saveApiKey(apiKey: String) = encryptedPrefs.saveApiKey(apiKey)

    override suspend fun clearApiKey() = encryptedPrefs.clearApiKey()

    override suspend fun updateDefaultModel(modelId: String, modelName: String) =
        dataStore.updateDefaultModel(modelId, modelName)

    override suspend fun updateDarkTheme(enabled: Boolean) = dataStore.updateDarkTheme(enabled)

    override suspend fun updateFontSize(size: Float) = dataStore.updateFontSize(size)

    override suspend fun updateNotificationsEnabled(enabled: Boolean) =
        dataStore.updateNotificationsEnabled(enabled)

    override suspend fun updateTtsSpeed(speed: Float) = dataStore.updateTtsSpeed(speed)

    override suspend fun updateTtsLanguage(language: String) =
        dataStore.updateTtsLanguage(language)
}
