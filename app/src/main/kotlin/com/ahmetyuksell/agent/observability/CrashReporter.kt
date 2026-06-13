package com.ahmetyuksell.agent.observability

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 5 — Observability: Crash and error reporting facade.
 * Currently logs to Timber. Add Firebase Crashlytics by implementing
 * the reportNonFatal / setUser / setKey methods with the SDK.
 */
@Singleton
class CrashReporter @Inject constructor() {

    fun reportNonFatal(throwable: Throwable, context: Map<String, String> = emptyMap()) {
        Timber.e(throwable, "Non-fatal error reported. Context: $context")
        // Future: FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    fun reportError(message: String, context: Map<String, String> = emptyMap()) {
        val exception = RuntimeException(message)
        Timber.e(exception, "Error: $message Context: $context")
        // Future: FirebaseCrashlytics.getInstance().recordException(exception)
    }

    fun setUserContext(userId: String?) {
        Timber.d("User context set: $userId")
        // Future: FirebaseCrashlytics.getInstance().setUserId(userId ?: "")
    }

    fun setKey(key: String, value: String) {
        Timber.d("Crash key: $key = $value")
        // Future: FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    fun log(message: String) {
        Timber.d("CrashReporter.log: $message")
        // Future: FirebaseCrashlytics.getInstance().log(message)
    }
}
