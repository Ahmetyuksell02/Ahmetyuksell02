package com.ahmetyuksell.agent.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.ahmetyuksell.agent.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StreamingForegroundService : Service() {

    @Inject lateinit var notificationHelper: NotificationHelper

    private val binder = StreamingBinder()
    private var isStreaming = false

    inner class StreamingBinder : Binder() {
        fun getService(): StreamingForegroundService = this@StreamingForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startStreaming()
            ACTION_STOP -> stopStreaming()
        }
        return START_NOT_STICKY
    }

    fun startStreaming() {
        if (isStreaming) return
        isStreaming = true
        startForeground(
            NotificationHelper.NOTIF_STREAMING,
            notificationHelper.buildStreamingNotification()
        )
    }

    fun stopStreaming() {
        isStreaming = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        isStreaming = false
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.ahmetyuksell.agent.STREAM_START"
        const val ACTION_STOP = "com.ahmetyuksell.agent.STREAM_STOP"
    }
}
