package com.ahmetyuksell.agent.data.repository

import com.ahmetyuksell.agent.data.local.dao.FileDao
import com.ahmetyuksell.agent.data.mapper.toDomain
import com.ahmetyuksell.agent.data.mapper.toEntity
import com.ahmetyuksell.agent.domain.model.AttachedFile
import com.ahmetyuksell.agent.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val dao: FileDao
) : FileRepository {

    override fun getFilesForConversation(conversationId: String): Flow<List<AttachedFile>> =
        dao.getFilesForConversation(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertFile(file: AttachedFile): String {
        dao.insertFile(file.toEntity())
        return file.id
    }

    override suspend fun getFile(id: String): AttachedFile? =
        dao.getFile(id)?.toDomain()

    override suspend fun updateOpenRouterFileId(id: String, openrouterFileId: String) {
        dao.updateOpenRouterFileId(id, openrouterFileId)
    }

    override suspend fun deleteFile(id: String) {
        dao.deleteFile(id)
    }
}
