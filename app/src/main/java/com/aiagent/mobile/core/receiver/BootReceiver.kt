package com.aiagent.mobile.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Receives BOOT_COMPLETED and MY_PACKAGE_REPLACED broadcasts to reschedule
 * periodic agent WorkManager tasks after a device reboot or app update.
 * Actual rescheduling logic is wired in Phase 4 (Agent System).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Timber.d("BootReceiver triggered: ${intent.action} — rescheduling agent tasks")
                // Agent task rescheduling implemented in Phase 4
            }
        }
    }
}
