package com.ahmetyuksell.agent.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createAll() {
        val manager = context.getSystemService<NotificationManager>() ?: return

        val streamingChannel = NotificationChannel(
            CHANNEL_STREAMING,
            context.getString(com.ahmetyuksell.agent.R.string.channel_streaming_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(com.ahmetyuksell.agent.R.string.channel_streaming_desc)
            setSound(null, null)
        }

        val agentChannel = NotificationChannel(
            CHANNEL_AGENT,
            context.getString(com.ahmetyuksell.agent.R.string.channel_agent_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(com.ahmetyuksell.agent.R.string.channel_agent_desc)
        }

        val generalChannel = NotificationChannel(
            CHANNEL_GENERAL,
            context.getString(com.ahmetyuksell.agent.R.string.channel_general_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(com.ahmetyuksell.agent.R.string.channel_general_desc)
        }

        manager.createNotificationChannels(listOf(streamingChannel, agentChannel, generalChannel))
    }

    companion object {
        const val CHANNEL_STREAMING = "streaming"
        const val CHANNEL_AGENT = "agent_tasks"
        const val CHANNEL_GENERAL = "general"
    }
}
