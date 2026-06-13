package com.ahmetyuksell.agent.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.ahmetyuksell.agent.MainActivity
import com.ahmetyuksell.agent.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager = context.getSystemService<NotificationManager>()

    fun buildStreamingNotification(): Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_STREAMING)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_streaming_title))
            .setContentText(context.getString(R.string.notification_streaming_text))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun buildAgentRunningNotification(agentName: String, taskId: String): Notification {
        val cancelIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("cancel_task_id", taskId)
        }
        val cancelPending = PendingIntent.getActivity(
            context, taskId.hashCode(), cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_AGENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_agent_running_title))
            .setContentText("$agentName is working…")
            .setProgress(0, 0, true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, context.getString(R.string.notification_cancel), cancelPending)
            .build()
    }

    fun showAgentCompletedNotification(agentName: String, result: String, notifId: Int) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_AGENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_agent_done_title))
            .setContentText("$agentName: ${result.take(80)}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(result.take(500)))
            .setAutoCancel(true)
            .build()
        manager?.notify(notifId, notification)
    }

    fun cancelNotification(notifId: Int) {
        manager?.cancel(notifId)
    }

    companion object {
        const val NOTIF_STREAMING = 1001
        const val NOTIF_AGENT_BASE = 2000
    }
}
