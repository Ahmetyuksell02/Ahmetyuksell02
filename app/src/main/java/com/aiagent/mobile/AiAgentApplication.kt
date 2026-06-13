package com.aiagent.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aiagent.mobile.core.common.Constants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AiAgentApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        initTimber()
        createNotificationChannels()
    }

    /**
     * Provide a custom WorkManager configuration so Hilt can inject
     * dependencies into Worker subclasses via HiltWorkerFactory.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val agentChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_AGENT,
                getString(R.string.notification_channel_agent_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_agent_description)
            }

            val chatChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_CHAT,
                getString(R.string.notification_channel_chat_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_chat_description)
            }

            notificationManager.createNotificationChannels(listOf(agentChannel, chatChannel))
        }
    }
}
