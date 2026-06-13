package com.ahmetyuksell.agent.data.remote.interceptor

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object LoggingInterceptor {

    fun create(): HttpLoggingInterceptor = HttpLoggingInterceptor { message ->
        val redacted = message
            .replace(Regex("Bearer [A-Za-z0-9\\-._~+/]+=*"), "Bearer [REDACTED]")
        Timber.tag("OkHttp").d(redacted)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }
}
