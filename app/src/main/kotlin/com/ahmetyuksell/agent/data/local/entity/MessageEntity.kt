package com.ahmetyuksell.agent.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversation_id"), Index("timestamp")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    val role: String,
    val content: String,
    @ColumnInfo(name = "model_id") val modelId: String?,
    val timestamp: Long,
    @ColumnInfo(name = "token_count") val tokenCount: Int?,
    @ColumnInfo(name = "is_streaming") val isStreaming: Boolean = false,
    @ColumnInfo(name = "tool_call_id") val toolCallId: String?,
    @ColumnInfo(name = "tool_call_name") val toolCallName: String?,
    @ColumnInfo(name = "tool_call_args_json") val toolCallArgsJson: String?,
    @ColumnInfo(name = "tool_result_json") val toolResultJson: String?,
    @ColumnInfo(name = "file_ids_json") val fileIdsJson: String?
)
