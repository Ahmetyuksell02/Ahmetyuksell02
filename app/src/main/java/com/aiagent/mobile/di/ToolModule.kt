package com.aiagent.mobile.di

import com.aiagent.mobile.core.common.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ToolModule {

    /**
     * Plain OkHttpClient for external tool APIs (no OpenRouter auth interceptors).
     * Used by FinanceTool, NewsTool, WebSearchTool.
     */
    @Provides
    @Singleton
    @Named("tool")
    fun provideToolHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(15L, TimeUnit.SECONDS)
            .build()
}
