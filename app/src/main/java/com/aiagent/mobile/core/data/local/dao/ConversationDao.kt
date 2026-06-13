package com.aiagent.mobile.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiagent.mobile.core.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAll(): Flow<List<ConversationEntity>>

    @Query("""
        SELECT * FROM conversations
        WHERE title LIKE '%' || :query || '%' OR lastMessage LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun search(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getById(id: String): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ConversationEntity)

    @Update
    suspend fun update(entity: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE conversations SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLastMessage(id: String, lastMessage: String, updatedAt: Long)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)
}
