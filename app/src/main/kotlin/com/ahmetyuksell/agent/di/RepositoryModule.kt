package com.ahmetyuksell.agent.di

import com.ahmetyuksell.agent.data.repository.*
import com.ahmetyuksell.agent.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds @Singleton
    abstract fun bindAiModelRepository(impl: AiModelRepositoryImpl): AiModelRepository

    @Binds @Singleton
    abstract fun bindAgentRepository(impl: AgentRepositoryImpl): AgentRepository

    @Binds @Singleton
    abstract fun bindAgentTaskRepository(impl: AgentTaskRepositoryImpl): AgentTaskRepository

    @Binds @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository
}
