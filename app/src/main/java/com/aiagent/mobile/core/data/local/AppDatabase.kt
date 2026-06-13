package com.aiagent.mobile.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.local.dao.AgentExecutionLogDao
import com.aiagent.mobile.core.data.local.dao.AgentTaskDao
import com.aiagent.mobile.core.data.local.dao.ConversationDao
import com.aiagent.mobile.core.data.local.dao.MessageDao
import com.aiagent.mobile.core.data.local.entity.AgentExecutionLogEntity
import com.aiagent.mobile.core.data.local.entity.AgentTaskEntity
import com.aiagent.mobile.core.data.local.entity.ConversationEntity
import com.aiagent.mobile.core.data.local.entity.MessageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        AgentTaskEntity::class,
        AgentExecutionLogEntity::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun agentTaskDao(): AgentTaskDao
    abstract fun agentExecutionLogDao(): AgentExecutionLogDao
}
