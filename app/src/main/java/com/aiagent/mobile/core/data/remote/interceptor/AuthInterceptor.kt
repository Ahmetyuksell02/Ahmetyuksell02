package com.aiagent.mobile.core.data.remote.interceptor

import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.preferences.EncryptedPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the OpenRouter API key from EncryptedSharedPreferences to every
 * outbound request. The key is read at request time so Settings changes
 * take effect immediately without recreating the OkHttpClient.
 */
class AuthInterceptor @Inject constructor(
    private val prefs: EncryptedPreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = prefs.getApiKey()
        val request = chain.request().newBuilder()
            .apply {
                if (apiKey.isNotBlank()) {
                    addHeader(Constants.HEADER_AUTHORIZATION, "Bearer $apiKey")
                }
                addHeader(Constants.HEADER_HTTP_REFERER, Constants.APP_SITE_URL)
                addHeader(Constants.HEADER_X_TITLE, Constants.APP_TITLE)
            }
            .build()
        return chain.proceed(request)
    }
}
