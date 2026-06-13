package com.ahmetyuksell.agent.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "files",
    indices = [Index("conversation_id")]
)
data class FileEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String?,
    @ColumnInfo(name = "local_uri") val localUri: String,
    val name: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "sha256_hash") val sha256Hash: String?,
    @ColumnInfo(name = "openrouter_file_id") val openrouterFileId: String?,
    @ColumnInfo(name = "uploaded_at") val uploadedAt: Long
)
