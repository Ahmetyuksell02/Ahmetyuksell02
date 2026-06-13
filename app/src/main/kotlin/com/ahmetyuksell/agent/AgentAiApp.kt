package com.ahmetyuksell.agent

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ahmetyuksell.agent.domain.repository.AgentTaskRepository
import com.ahmetyuksell.agent.notification.NotificationChannels
import com.ahmetyuksell.agent.worker.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AgentAiApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationChannels: NotificationChannels
    @Inject lateinit var workManagerScheduler: WorkManagerScheduler
    @Inject lateinit var agentTaskRepository: AgentTaskRepository

    // Application-scoped coroutine scope for fire-and-forget startup work
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMaxSchedulerLimit(10)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        notificationChannels.createAll()
        workManagerScheduler.scheduleModelSync()
        repairOrphanedTasks()
    }

    // Tasks left as RUNNING when the app was killed are stuck forever unless we reset them.
    // WorkManager will re-enqueue them if they were scheduled, so marking FAILED is safe —
    // the worker's idempotency guard will handle any in-progress WorkManager retries cleanly.
    private fun repairOrphanedTasks() {
        appScope.launch {
            try {
                val repaired = agentTaskRepository.resetOrphanedRunningTasks()
                if (repaired > 0) {
                    Timber.w("Boot recovery: reset $repaired orphaned RUNNING/PENDING tasks to FAILED")
                }
            } catch (e: Exception) {
                Timber.e(e, "Boot recovery: failed to reset orphaned tasks")
            }
        }
    }
}
