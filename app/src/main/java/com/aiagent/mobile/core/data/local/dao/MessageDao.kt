package com.aiagent.mobile.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiagent.mobile.core.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getByConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MessageEntity)

    @Update
    suspend fun update(entity: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversation(conversationId: String)

    @Query("UPDATE messages SET content = :content WHERE id = :id")
    suspend fun updateContent(id: String, content: String)

    @Query("UPDATE messages SET isError = :isError WHERE id = :id")
    suspend fun setError(id: String, isError: Boolean)
}
