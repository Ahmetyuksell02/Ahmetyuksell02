package com.ahmetyuksell.agent.data.local.dao

import androidx.room.*
import com.ahmetyuksell.agent.data.local.entity.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Query("SELECT * FROM files WHERE conversation_id = :conversationId ORDER BY uploaded_at DESC")
    fun getFilesForConversation(conversationId: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFile(id: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(entity: FileEntity)

    @Query("UPDATE files SET openrouter_file_id = :openrouterFileId WHERE id = :id")
    suspend fun updateOpenRouterFileId(id: String, openrouterFileId: String)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFile(id: String)
}
