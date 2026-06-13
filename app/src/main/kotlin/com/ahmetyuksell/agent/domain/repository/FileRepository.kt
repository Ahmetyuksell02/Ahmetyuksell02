package com.ahmetyuksell.agent.domain.repository

import com.ahmetyuksell.agent.domain.model.AttachedFile
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun getFilesForConversation(conversationId: String): Flow<List<AttachedFile>>
    suspend fun insertFile(file: AttachedFile): String
    suspend fun getFile(id: String): AttachedFile?
    suspend fun updateOpenRouterFileId(id: String, openrouterFileId: String)
    suspend fun deleteFile(id: String)
}
