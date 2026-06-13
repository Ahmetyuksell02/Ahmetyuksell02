package com.aiagent.mobile.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val DEFAULT_MODEL_ID = stringPreferencesKey(Constants.PREF_DEFAULT_MODEL)
        val DEFAULT_MODEL_NAME = stringPreferencesKey("pref_default_model_name")
        val DARK_THEME = booleanPreferencesKey(Constants.PREF_DARK_THEME)
        val FONT_SIZE = floatPreferencesKey(Constants.PREF_FONT_SIZE)
        val NOTIFICATIONS = booleanPreferencesKey(Constants.PREF_NOTIFICATIONS_ENABLED)
        val TTS_SPEED = floatPreferencesKey(Constants.PREF_TTS_SPEED)
        val TTS_LANGUAGE = stringPreferencesKey(Constants.PREF_TTS_LANGUAGE)
    }

    val settings: Flow<UserSettings> = dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            UserSettings(
                defaultModelId = prefs[Keys.DEFAULT_MODEL_ID] ?: Constants.DEFAULT_MODEL_ID,
                defaultModelName = prefs[Keys.DEFAULT_MODEL_NAME] ?: Constants.DEFAULT_MODEL_NAME,
                isDarkTheme = prefs[Keys.DARK_THEME] ?: false,
                fontSize = prefs[Keys.FONT_SIZE] ?: Constants.DEFAULT_FONT_SIZE,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
                ttsSpeed = prefs[Keys.TTS_SPEED] ?: Constants.DEFAULT_TTS_SPEED,
                ttsLanguage = prefs[Keys.TTS_LANGUAGE] ?: Constants.DEFAULT_TTS_LANGUAGE
            )
        }

    suspend fun updateDefaultModel(modelId: String, modelName: String) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_MODEL_ID] = modelId
            prefs[Keys.DEFAULT_MODEL_NAME] = modelName
        }
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun updateFontSize(size: Float) {
        dataStore.edit { it[Keys.FONT_SIZE] = size }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun updateTtsSpeed(speed: Float) {
        dataStore.edit { it[Keys.TTS_SPEED] = speed }
    }

    suspend fun updateTtsLanguage(language: String) {
        dataStore.edit { it[Keys.TTS_LANGUAGE] = language }
    }
}
