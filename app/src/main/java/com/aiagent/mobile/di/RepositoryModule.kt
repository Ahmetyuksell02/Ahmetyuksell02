package com.aiagent.mobile.di

import com.aiagent.mobile.core.data.repository.AgentTaskRepositoryImpl
import com.aiagent.mobile.core.data.repository.ConversationRepositoryImpl
import com.aiagent.mobile.core.data.repository.MessageRepositoryImpl
import com.aiagent.mobile.core.data.repository.ModelRepositoryImpl
import com.aiagent.mobile.core.data.repository.SettingsRepositoryImpl
import com.aiagent.mobile.core.domain.repository.IAgentTaskRepository
import com.aiagent.mobile.core.domain.repository.IConversationRepository
import com.aiagent.mobile.core.domain.repository.IMessageRepository
import com.aiagent.mobile.core.domain.repository.IModelRepository
import com.aiagent.mobile.core.domain.repository.ISettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl
    ): IConversationRepository

    @Binds @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): IMessageRepository

    @Binds @Singleton
    abstract fun bindAgentTaskRepository(
        impl: AgentTaskRepositoryImpl
    ): IAgentTaskRepository

    @Binds @Singleton
    abstract fun bindModelRepository(
        impl: ModelRepositoryImpl
    ): IModelRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): ISettingsRepository
}
