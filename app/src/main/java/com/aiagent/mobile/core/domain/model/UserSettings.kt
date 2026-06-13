package com.aiagent.mobile.core.domain.model

import com.aiagent.mobile.core.common.Constants

data class UserSettings(
    val defaultModelId: String = Constants.DEFAULT_MODEL_ID,
    val defaultModelName: String = Constants.DEFAULT_MODEL_NAME,
    val isDarkTheme: Boolean = false,
    val fontSize: Float = Constants.DEFAULT_FONT_SIZE,
    val notificationsEnabled: Boolean = true,
    val ttsSpeed: Float = Constants.DEFAULT_TTS_SPEED,
    val ttsLanguage: String = Constants.DEFAULT_TTS_LANGUAGE
)
