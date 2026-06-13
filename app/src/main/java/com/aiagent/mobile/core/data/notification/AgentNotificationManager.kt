package com.aiagent.mobile.core.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aiagent.mobile.MainActivity
import com.aiagent.mobile.R
import com.aiagent.mobile.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationIdCounter = AtomicInteger(Constants.NOTIFICATION_ID_AGENT_BASE)

    fun showCompletionNotification(agentTitle: String, resultSummary: String) {
        if (!hasPermission()) return

        val notificationId = notificationIdCounter.incrementAndGet()
        val shortSummary = resultSummary.take(200).let {
            if (resultSummary.length > 200) "$it…" else it
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_AGENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✓ $agentTitle completed")
            .setContentText(shortSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(shortSummary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: Exception) {
            Timber.w(e, "Failed to show completion notification")
        }
    }

    fun showFailureNotification(agentTitle: String, error: String) {
        if (!hasPermission()) return

        val notificationId = notificationIdCounter.incrementAndGet()

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_AGENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✗ $agentTitle failed")
            .setContentText(error.take(120))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: Exception) {
            Timber.w(e, "Failed to show failure notification")
        }
    }

    fun showProgressNotification(agentTitle: String, progress: Float, message: String): Int {
        if (!hasPermission()) return -1

        val notificationId = Constants.NOTIFICATION_ID_AGENT_BASE

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_AGENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(agentTitle)
            .setContentText(message)
            .setProgress(100, (progress * 100).toInt(), false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: Exception) {
            Timber.w(e, "Failed to show progress notification")
        }
        return notificationId
    }

    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun hasPermission(): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
}
