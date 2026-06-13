package com.aiagent.mobile.core.common

object Constants {

    // ─── OpenRouter API ───────────────────────────────────────────────────────
    const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"
    const val OPENROUTER_MODELS_ENDPOINT = "models"
    const val OPENROUTER_CHAT_ENDPOINT = "chat/completions"

    // ─── Room Database ────────────────────────────────────────────────────────
    const val DATABASE_NAME = "ai_agent_db"
    const val DATABASE_VERSION = 2

    // ─── WorkManager Tags ─────────────────────────────────────────────────────
    const val AGENT_TASK_WORKER_TAG = "agent_task_worker"
    const val RESCHEDULE_WORKER_TAG = "reschedule_agents_worker"
    const val DAILY_SUMMARY_WORKER_TAG = "daily_summary_worker"
    const val PERIODIC_MONITOR_WORKER_TAG = "periodic_monitor_worker"
    const val KEY_TASK_ID = "task_id"
    const val KEY_PROGRESS = "progress"
    const val KEY_PROGRESS_MESSAGE = "progress_message"
    const val KEY_ERROR = "error_message"

    // ─── Agent Execution ──────────────────────────────────────────────────────
    const val MAX_AGENT_EXECUTION_MS = 8L * 60L * 1000L  // 8 minutes (WorkManager cap is ~10)
    const val MIN_PERIODIC_INTERVAL_MINUTES = 15L
    const val AGENT_BACKOFF_DELAY_SECONDS = 30L
    const val AGENT_DEFAULT_MAX_RETRIES = 3
    const val AGENT_MAX_TOKENS = 2048
    const val AGENT_RATE_LIMIT_MS = 5L * 60L * 1000L   // 5 minutes between same-task runs

    // ─── DataStore ────────────────────────────────────────────────────────────
    const val USER_PREFERENCES_NAME = "user_preferences"

    // ─── Preference Keys ──────────────────────────────────────────────────────
    const val PREF_API_KEY = "pref_api_key"
    const val PREF_DEFAULT_MODEL = "pref_default_model"
    const val PREF_DARK_THEME = "pref_dark_theme"
    const val PREF_FONT_SIZE = "pref_font_size"
    const val PREF_NOTIFICATIONS_ENABLED = "pref_notifications_enabled"
    const val PREF_TTS_SPEED = "pref_tts_speed"
    const val PREF_TTS_LANGUAGE = "pref_tts_language"

    // ─── Defaults ─────────────────────────────────────────────────────────────
    const val DEFAULT_MODEL_ID = "openai/gpt-4o"
    const val DEFAULT_MODEL_NAME = "GPT-4o"
    const val DEFAULT_FONT_SIZE = 16f
    const val DEFAULT_TTS_SPEED = 1.0f
    const val DEFAULT_TTS_LANGUAGE = "en-US"
    const val DEFAULT_MAX_TOKENS = 4096
    const val STREAM_BUFFER_SIZE = 8192

    // ─── Notification Channels ────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_AGENT = "agent_notifications"
    const val NOTIFICATION_CHANNEL_CHAT = "chat_notifications"
    const val NOTIFICATION_ID_AGENT_BASE = 1000
    const val NOTIFICATION_ID_CHAT_BASE = 2000

    // ─── Conversation ─────────────────────────────────────────────────────────
    const val NEW_CONVERSATION_ID = "new"
    const val MAX_CONVERSATIONS_DISPLAYED = 100
    const val AUTO_TITLE_MAX_CHARS = 60

    // ─── Security ─────────────────────────────────────────────────────────────
    const val ENCRYPTED_PREFS_NAME = "ai_agent_secure_prefs"

    // ─── Network ──────────────────────────────────────────────────────────────
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 120L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // ─── HTTP Headers ─────────────────────────────────────────────────────────
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_HTTP_REFERER = "HTTP-Referer"
    const val HEADER_X_TITLE = "X-Title"
    const val APP_SITE_URL = "https://github.com/aiagent/mobile"
    const val APP_TITLE = "AI Agent Mobile"
}
