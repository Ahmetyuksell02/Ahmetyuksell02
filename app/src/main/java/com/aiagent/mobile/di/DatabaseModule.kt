package com.aiagent.mobile.di

import android.content.Context
import androidx.room.Room
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.local.AppDatabase
import com.aiagent.mobile.core.data.local.dao.AgentExecutionLogDao
import com.aiagent.mobile.core.data.local.dao.AgentTaskDao
import com.aiagent.mobile.core.data.local.dao.ConversationDao
import com.aiagent.mobile.core.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            // Use destructive migration during development; add real migrations before release
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()

    @Provides
    @Singleton
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun provideAgentTaskDao(db: AppDatabase): AgentTaskDao = db.agentTaskDao()

    @Provides
    @Singleton
    fun provideAgentExecutionLogDao(db: AppDatabase): AgentExecutionLogDao = db.agentExecutionLogDao()
}
