package com.ahmetyuksell.agent.data.remote.interceptor

import com.ahmetyuksell.agent.security.SecureKeyStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val secureKeyStore: SecureKeyStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = secureKeyStore.getApiKey()
        val request = chain.request().newBuilder().apply {
            if (!apiKey.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $apiKey")
            }
            addHeader("HTTP-Referer", "https://github.com/ahmetyuksell02/AgentAI")
            addHeader("X-Title", "AgentAI")
            addHeader("Content-Type", "application/json")
        }.build()
        return chain.proceed(request)
    }
}
