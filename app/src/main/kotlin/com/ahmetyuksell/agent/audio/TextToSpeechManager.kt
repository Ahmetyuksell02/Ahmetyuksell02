package com.ahmetyuksell.agent.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val isReady = AtomicBoolean(false)
    private var utteranceCounter = 0

    init {
        initialize()
    }

    private fun initialize() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) = Unit
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = Unit
                })
                isReady.set(true)
            } else {
                Timber.e("TTS initialization failed with status $status")
            }
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isReady.get()) return
        val utteranceId = "utt_${utteranceCounter++}"
        tts?.speak(text, queueMode, null, utteranceId)
    }

    fun speakQueued(text: String) {
        speak(text, TextToSpeech.QUEUE_ADD)
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun setLanguage(locale: Locale) {
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Timber.w("TTS language ${locale.language} not supported")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady.set(false)
    }

    fun splitIntoSentences(text: String): List<String> =
        text.split(Regex("(?<=[.!?])\\s+"))
            .filter { it.isNotBlank() }
}
