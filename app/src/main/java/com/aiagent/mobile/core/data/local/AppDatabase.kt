package com.aiagent.mobile.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiagent.mobile.core.data.local.dao.AgentTaskDao
import com.aiagent.mobile.core.data.local.dao.ConversationDao
import com.aiagent.mobile.core.data.local.dao.MessageDao
import com.aiagent.mobile.core.data.local.entity.AgentTaskEntity
import com.aiagent.mobile.core.data.local.entity.ConversationEntity
import com.aiagent.mobile.core.data.local.entity.MessageEntity
import com.aiagent.mobile.core.common.Constants

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        AgentTaskEntity::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun agentTaskDao(): AgentTaskDao
}
