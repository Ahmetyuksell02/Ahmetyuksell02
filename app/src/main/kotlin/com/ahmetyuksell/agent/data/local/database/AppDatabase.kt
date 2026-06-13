package com.ahmetyuksell.agent.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ahmetyuksell.agent.data.local.dao.*
import com.ahmetyuksell.agent.data.local.entity.*

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        AiModelEntity::class,
        AgentEntity::class,
        AgentTaskEntity::class,
        FileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun aiModelDao(): AiModelDao
    abstract fun agentDao(): AgentDao
    abstract fun agentTaskDao(): AgentTaskDao
    abstract fun fileDao(): FileDao

    companion object {
        const val DATABASE_NAME = "agent_ai_db"
    }
}
