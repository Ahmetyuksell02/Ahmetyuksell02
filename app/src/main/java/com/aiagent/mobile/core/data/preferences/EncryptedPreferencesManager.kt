package com.aiagent.mobile.core.data.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aiagent.mobile.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Android EncryptedSharedPreferences (AES-256-GCM) for storing the
 * OpenRouter API key securely. Key material is backed by Android Keystore.
 */
@Singleton
class EncryptedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            Constants.ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(Constants.PREF_API_KEY, apiKey.trim()).apply()
    }

    fun getApiKey(): String = prefs.getString(Constants.PREF_API_KEY, "") ?: ""

    fun clearApiKey() {
        prefs.edit().remove(Constants.PREF_API_KEY).apply()
    }

    fun hasApiKey(): Boolean = getApiKey().isNotBlank()
}
