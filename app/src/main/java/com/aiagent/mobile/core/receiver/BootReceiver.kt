package com.aiagent.mobile.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.worker.RescheduleAgentsWorker
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Timber.i("BootReceiver: ${intent.action} — scheduling agent recovery")

                val rescheduleRequest = OneTimeWorkRequestBuilder<RescheduleAgentsWorker>()
                    .addTag(Constants.RESCHEDULE_WORKER_TAG)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    Constants.RESCHEDULE_WORKER_TAG,
                    ExistingWorkPolicy.REPLACE,
                    rescheduleRequest
                )
            }
        }
    }
}
