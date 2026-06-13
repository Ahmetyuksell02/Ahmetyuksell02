package com.ahmetyuksell.agent.di

import android.content.Context
import androidx.room.Room
import com.ahmetyuksell.agent.data.local.database.AppDatabase
import com.ahmetyuksell.agent.data.local.dao.*
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()
    @Provides fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()
    @Provides fun provideAiModelDao(db: AppDatabase): AiModelDao = db.aiModelDao()
    @Provides fun provideAgentDao(db: AppDatabase): AgentDao = db.agentDao()
    @Provides fun provideAgentTaskDao(db: AppDatabase): AgentTaskDao = db.agentTaskDao()
    @Provides fun provideFileDao(db: AppDatabase): FileDao = db.fileDao()
}
